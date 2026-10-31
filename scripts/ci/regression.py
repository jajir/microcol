#!/usr/bin/env python3
"""Select the nightly main regression and publish its two test groups."""
import argparse
import html
import importlib.util
import io
import json
import os
import re
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location('ui_verify', ROOT / 'scripts/ui-tests/verify.py')
VERIFY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFY)
WORKFLOW = 'dev-builds.yml'
JOB = 'Functional player scenarios'
GROUPS = {'model': 'Model / JUnit', 'ui': 'Full UI / support'}


def api(path, binary=False):
    data = subprocess.check_output(['gh', 'api', path], timeout=60)
    return data if binary else json.loads(data)


def latest_run(repository, exclude=None):
    """Skip only runs whose functional job was explicitly skipped."""
    runs = api(f'repos/{repository}/actions/workflows/{WORKFLOW}/runs?branch=main&per_page=100')
    # shortcut: inspect 100 recent runs; paginate if longer idle history must be retained.
    for run in runs['workflow_runs']:
        if str(run['id']) == str(exclude) or run['head_branch'] != 'main':
            continue
        jobs = api(f'repos/{repository}/actions/runs/{run["id"]}/jobs?per_page=100')['jobs']
        job = next((item for item in jobs if item['name'] == JOB), None)
        if job and job['conclusion'] == 'skipped':
            continue
        return run, job
    return None, None


def needs_regression(previous, revision, root=ROOT):
    if not previous or not re.fullmatch(r'[0-9a-f]{40}', previous) or not re.fullmatch(r'[0-9a-f]{40}', revision):
        return True
    if previous == revision:
        return False
    try:
        paths = subprocess.check_output(
            ['git', 'diff', '--no-renames', '--name-only', '-z', previous, revision, '--'],
            cwd=root, stderr=subprocess.PIPE).split(b'\0')
    except subprocess.CalledProcessError:
        return True
    # Match the existing conservative docs-only classifier, but allow an empty diff.
    return any(path and not path.startswith(b'docs/') for path in paths)


def plan(repository):
    required = True
    if os.environ['GITHUB_EVENT_NAME'] == 'schedule':
        run, job = latest_run(repository, os.environ.get('GITHUB_RUN_ID'))
        previous = run['head_sha'] if run and run['status'] == 'completed' else None
        required = needs_regression(previous, os.environ['GITHUB_SHA'])
    line = f'game_required={str(required).lower()}\n'
    print(line, end='')
    with open(os.environ['GITHUB_OUTPUT'], 'a') as destination:
        destination.write(line)


def collect_group(directory, expected):
    result = {'passed': 0, 'failed': 0, 'errors': 0, 'skipped': 0,
              'state': 'incomplete', 'problems': []}
    try:
        for path in sorted(directory.rglob('TEST-*.xml')):
            try:
                suite = ET.parse(path).getroot()
            except (OSError, ET.ParseError):
                # An interrupted XML write must not hide other completed suites.
                continue
            for case in suite.findall('testcase'):
                outcome = next((key for key, tag in (('failed', 'failure'), ('errors', 'error'),
                                                     ('skipped', 'skipped'))
                                if case.find(tag) is not None), 'passed')
                result[outcome] += 1
                if outcome != 'passed':
                    element = case.find({'failed': 'failure', 'errors': 'error', 'skipped': 'skipped'}[outcome])
                    result['problems'].append({
                        'test': case.get('classname', suite.get('name', '')) + '.' + case.get('name', ''),
                        'outcome': outcome, 'message': element.get('message', '')[:2000]})
        VERIFY.validate_reports(directory, expected)
        result['state'] = 'passed'
    except (OSError, ValueError, KeyError, ET.ParseError) as error:
        result['reason'] = str(error)
        if result['failed'] or result['errors']:
            result['state'] = 'failed'
    return result


def summarize(directory, revision):
    result = {'revision': revision, 'attempt': int(os.environ.get('GITHUB_RUN_ATTEMPT', '1')), 'groups': {}}
    manifests = list(directory.glob('run-*/manifest.json'))
    if len(manifests) != 1:
        result['error'] = 'Expected one fresh verification manifest; no complete test result is available.'
        return result
    root = manifests[0].parent
    manifest = json.loads(manifests[0].read_text())
    if manifest['source']['revision'] != revision or manifest['scope'] != 'full':
        raise ValueError('Report is not a full regression of the requested revision')
    summary = json.loads((root / 'summary.json').read_text()) if (root / 'summary.json').exists() else {}
    for key, module, folder in (('model', 'microcol-game', 'model'), ('ui', 'microcol-test', 'ui-01')):
        group = collect_group(root / folder, manifest['expected'][module])
        outcome = root / folder / 'result.json'
        process = json.loads(outcome.read_text()) if outcome.exists() else {}
        if group['state'] == 'passed' and process.get('status') != 'passed':
            group.update(state='incomplete', reason=process.get('error', 'Missing successful process result'))
        result['groups'][key] = group
    if summary.get('status') != 'passed':
        result['error'] = summary.get('error', 'Verification did not finish successfully')
    return result


def read_summary(repository, run):
    artifacts = api(f'repos/{repository}/actions/runs/{run["id"]}/artifacts?per_page=100')['artifacts']
    artifact = next((item for item in artifacts if item['name'] == f'test-summary-{run.get("run_attempt", 1)}' and not item['expired']), None)
    if not artifact:
        return {'groups': {}, 'error': 'Test summary is missing or expired. See the workflow logs.'}
    data = api(f'repos/{repository}/actions/artifacts/{artifact["id"]}/zip', binary=True)
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        summary = json.loads(archive.read('summary.json'))
    if not isinstance(summary, dict) or not isinstance(summary.get('groups'), dict):
        raise ValueError('Invalid test summary structure')
    if (summary.get('revision') != run['head_sha']
            or summary.get('attempt') != run.get('run_attempt', 1)):
        raise ValueError('Summary revision/attempt does not match the workflow run')
    for group in summary['groups'].values():
        if group['state'] not in ('passed', 'failed', 'incomplete'):
            raise ValueError('Invalid test group state')
        for field in ('passed', 'failed', 'errors', 'skipped'):
            if type(group[field]) is not int or group[field] < 0:
                raise ValueError('Invalid test count')
        if group['state'] == 'passed' and (not group['passed'] or any(group[k] for k in ('failed', 'errors', 'skipped'))):
            raise ValueError('Contradictory passing result')
    return summary


def badge(label, message, color):
    # Plain vector text keeps the badges self-hosted on the existing Pages site.
    left, right = len(label) * 7 + 20, len(message) * 7 + 20
    label, message = html.escape(label), html.escape(message)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{left + right}" height="24" '
            f'role="img" aria-label="{label}: {message}"><title>{label}: {message}</title>'
            f'<rect width="{left + right}" height="24" rx="3" fill="#{color}"/>'
            f'<path fill="#354052" d="M3 0h{left - 3}v24H3Q0 24 0 21V3Q0 0 3 0"/>'
            '<g fill="white" text-anchor="middle" font-family="Verdana,Arial,sans-serif" font-size="11">'
            f'<text x="{left / 2}" y="16">{label}</text>'
            f'<text x="{left + right / 2}" y="16">{message}</text></g></svg>\n')


def render(summary, run, current, job=None):
    lines = []
    if run:
        url = run['html_url']
        lines += [f'Latest full regression of `main`: **{run["head_sha"]}**.', '',
                  f'Started: **{run.get("run_started_at", run["created_at"])}** (UTC). '
                  f'[Workflow, job logs and downloadable evidence]({url}).', '']
        if run['head_sha'] != current:
            lines += ['**Newer main is not covered by this full regression.** '
                      f'The documentation source is `{current}`.', '']
    elif summary.get('groups'):
        lines += [f'Tested revision: `{summary["revision"]}`.', '']
    else:
        lines += ['**No full regression result is available.**', '']
    if summary.get('error'):
        lines += [html.escape(summary['error']), '']
    lines += ['| Group | Passed | Failed | Errors | Skipped | Result |',
              '| --- | ---: | ---: | ---: | ---: | --- |']
    badges = {}
    for key, label in GROUPS.items():
        group = summary.get('groups', {}).get(key)
        state = group['state'] if group else 'unknown'
        if job and job['status'] != 'completed':
            state = 'running'
        elif job and job['conclusion'] != 'success' and state == 'passed':
            state = 'incomplete'
        stale = run and run['head_sha'] != current
        color = 'b42318' if state == 'failed' else '1a7f37' if state == 'passed' and not stale else '9a6700'
        if group and sum(group[name] for name in ('passed', 'failed', 'errors', 'skipped')):
            cells = ' | '.join(str(group[name]) for name in ('passed', 'failed', 'errors', 'skipped'))
            message = f'{group["passed"]} pass / {group["failed"] + group["errors"]} fail / {group["skipped"]} skip'
            if state != 'passed':
                message += f' - {state}'
        else:
            cells, message = '— | — | — | —', state
        if stale:
            message += ' - older main'
        badges[key] = badge(label, message, color)
        lines.append(f'| {label} | {cells} | {state} |')
    for key, label in GROUPS.items():
        group = summary.get('groups', {}).get(key, {})
        lines += ['', f'### {label}', '']
        if group.get('reason'):
            lines += [html.escape(group['reason']), '']
        for problem in group.get('problems', []):
            lines += [f'- **{html.escape(problem["outcome"])}**: {html.escape(problem["test"])}',
                      f'  <pre>{html.escape(problem["message"])}</pre>']
        if not group.get('problems'):
            lines += ['No failing or skipped test cases were reported. This alone does not establish a complete pass.']
    return '\n'.join(lines) + '\n', badges


def publish(repository, current, destination):
    run = job = None
    try:
        run, job = latest_run(repository)
        summary = read_summary(repository, run) if run else {'groups': {}}
        markdown, badges = render(summary, run, current, job)
    except (OSError, ValueError, KeyError, TypeError, zipfile.BadZipFile, subprocess.SubprocessError) as error:
        summary = {'groups': {}, 'error': f'Unable to retrieve verified test results: {error}'}
        markdown, badges = render(summary, run, current, job)
    destination.mkdir(parents=True, exist_ok=True)
    (destination / 'report.md').write_text(markdown)
    for key, image in badges.items():
        (destination / f'test-results-{key}.svg').write_text(image)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=('plan', 'summarize', 'publish'))
    parser.add_argument('--repository', default=os.environ.get('GITHUB_REPOSITORY', 'jajir/microcol'))
    parser.add_argument('--revision', default=os.environ.get('GITHUB_SHA'))
    parser.add_argument('--directory', type=Path, default=ROOT / 'target/verification')
    parser.add_argument('--output', type=Path, default=ROOT / 'target/test-status')
    args = parser.parse_args()
    if args.command == 'plan':
        plan(args.repository)
    elif args.command == 'summarize':
        summary = summarize(args.directory, args.revision)
        args.output.mkdir(parents=True, exist_ok=True)
        (args.output / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n')
        markdown, _ = render(summary, None, args.revision)
        if os.environ.get('GITHUB_STEP_SUMMARY'):
            with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as destination:
                destination.write(markdown)
    else:
        publish(args.repository, args.revision, args.output)


if __name__ == '__main__':
    main()
