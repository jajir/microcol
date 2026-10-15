#!/usr/bin/env python3
"""Skip automatic game work only for a known, nonempty docs/ change set."""
import json
import os
from pathlib import Path
import re
import subprocess


def game_required(event_name, event, repository='.'):
    if not isinstance(event, dict):
        return True
    try:
        if event_name == 'push':
            base, head = event['before'], event['after']
            separator = '..'
        elif event_name == 'pull_request':
            base = event['pull_request']['base']['sha']
            head = event['pull_request']['head']['sha']
            separator = '...'
        else:
            return True
        for sha in (base, head):
            if (not isinstance(sha, str) or not re.fullmatch(r'[0-9a-fA-F]{40}', sha)
                    or sha == '0' * 40):
                return True
        result = subprocess.run(
            ['git', 'diff', '--no-renames', '--name-only', '-z',
             base + separator + head, '--'], cwd=repository,
            check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        paths = result.stdout.split(b'\0')
        if paths[-1] != b'':
            return True
        paths = paths[:-1]
        return not paths or any(not path.startswith(b'docs/') for path in paths)
    except (KeyError, TypeError, OSError, subprocess.CalledProcessError):
        return True


def main():
    try:
        event = json.loads(Path(os.environ['GITHUB_EVENT_PATH']).read_text())
    except (KeyError, OSError, ValueError, UnicodeError):
        event = None
    required = game_required(os.environ.get('GITHUB_EVENT_NAME'), event)
    output = 'game_required=' + str(required).lower() + '\n'
    print(output, end='')
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'], 'a') as destination:
            destination.write(output)


if __name__ == '__main__':
    main()
