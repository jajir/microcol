import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

from game_changes import game_required

SCRIPT = Path(__file__).with_name('game_changes.py').resolve()


class ChangeTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = Path(self.temp.name)
        self.git('init', '-q')
        self.git('config', 'user.name', 'Test')
        self.git('config', 'user.email', 'test@example.invalid')
        self.write('docs/old.md')
        self.write('code.java')
        self.base = self.commit()

    def git(self, *args):
        return subprocess.check_output(['git', *args], cwd=self.repo,
                                       stderr=subprocess.PIPE).decode().strip()

    def write(self, path, content='content\n'):
        file = self.repo / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content)

    def commit(self):
        self.git('add', '-A')
        self.git('commit', '-qm', 'change')
        return self.git('rev-parse', 'HEAD')

    def push(self, base=None, head=None):
        return game_required('push', {'before': base or self.base,
                                     'after': head or self.commit()}, self.repo)

    def test_nested_addition_and_deletion(self):
        self.write('docs/nested/new.md')
        (self.repo / 'docs/old.md').unlink()
        self.assertFalse(self.push())

    def test_code_only(self):
        self.write('code.java', 'changed')
        self.assertTrue(self.push())

    def test_mixed(self):
        self.write('docs/new.md')
        self.write('code.java', 'changed')
        self.assertTrue(self.push())

    def test_rename_within_docs(self):
        (self.repo / 'docs/old.md').rename(self.repo / 'docs/new.md')
        self.assertFalse(self.push())

    def test_rename_out_of_docs(self):
        (self.repo / 'docs/old.md').rename(self.repo / 'outside.md')
        self.assertTrue(self.push())

    def test_rename_into_docs(self):
        (self.repo / 'code.java').rename(self.repo / 'docs/code.md')
        self.assertTrue(self.push())

    def test_all_push_commits_count(self):
        self.write('code.java', 'changed')
        self.commit()
        self.write('docs/last.md')
        self.assertTrue(self.push())

    def test_diverged_pr_uses_merge_base(self):
        self.git('checkout', '-qb', 'topic')
        self.write('docs/topic.md')
        head = self.commit()
        self.git('checkout', '--detach', self.base)
        self.write('code.java', 'unrelated target change')
        base = self.commit()
        self.assertFalse(game_required('pull_request', {
            'pull_request': {'base': {'sha': base}, 'head': {'sha': head}}}, self.repo))

    def test_unusual_docs_paths(self):
        self.write('docs/a b\nline.md')
        self.write('docs/é.md')
        self.assertFalse(self.push())

    def test_unusual_outside_path_is_not_trimmed(self):
        self.write(' docs/not-docs\n.md')
        self.assertTrue(self.push())

    def test_empty_diff_and_missing_history(self):
        self.assertTrue(self.push(head=self.base))
        self.assertTrue(self.push(head='f' * 40))

    def test_manual_unknown_and_invalid_data(self):
        valid = {'before': self.base, 'after': self.base}
        for name in ('workflow_dispatch', 'unknown', None):
            with self.subTest(name=name):
                self.assertTrue(game_required(name, valid, self.repo))
        for payload in (None, [], {}, {'before': []},
                        {'before': '0' * 40, 'after': self.base},
                        {'before': '--help', 'after': self.base},
                        {'before': self.base, 'after': 123}):
            with self.subTest(payload=payload):
                self.assertTrue(game_required('push', payload, self.repo))
        for pr in (None, [], {}, {'base': None}, {'base': {'sha': self.base}, 'head': []}):
            self.assertTrue(game_required('pull_request', {'pull_request': pr}, self.repo))
        self.assertTrue(game_required('push', valid, self.repo / 'missing'))

    def test_cli_output_and_bad_event_files(self):
        self.write('docs/cli.md')
        head = self.commit()
        event = self.repo / 'event.json'
        output = self.repo / 'output.txt'
        environment = {**os.environ, 'GITHUB_EVENT_NAME': 'push',
                       'GITHUB_EVENT_PATH': str(event), 'GITHUB_OUTPUT': str(output)}
        for contents, expected in ((json.dumps({'before': self.base, 'after': head}), 'false'),
                                   ('{', 'true'), ('[]', 'true'), (None, 'true')):
            with self.subTest(contents=contents):
                if contents is None:
                    event.unlink()
                else:
                    event.write_text(contents)
                output.write_text('earlier=preserved\n')
                result = subprocess.run(['python3', str(SCRIPT)], cwd=self.repo,
                                        env=environment, capture_output=True, check=True, text=True)
                line = 'game_required=' + expected + '\n'
                self.assertEqual(line, result.stdout)
                self.assertEqual('earlier=preserved\n' + line, output.read_text())


if __name__ == '__main__':
    unittest.main()
