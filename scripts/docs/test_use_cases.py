"""Keep the manual-rule and player-journey catalogues connected."""

import re
import unittest
from pathlib import Path


class UseCaseCatalogueTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        docs = Path(__file__).resolve().parents[2] / "docs"
        cls.rules = (docs / "gap-analysis.md").read_text(encoding="utf-8")
        cls.cases = (docs / "use-cases.md").read_text(encoding="utf-8")
        cls.rows = [line for line in cls.cases.splitlines()
                    if re.match(r"\| UC-\d{3} —", line)]

    def test_unique_cases_and_complete_rule_coverage(self):
        rule_ids = re.findall(r"^\| ([A-Z]+-\d{3}) \|", self.rules, re.M)
        case_ids = [re.match(r"\| (UC-\d{3})", row)[1] for row in self.rows]
        self.assertTrue(rule_ids)
        self.assertTrue(case_ids)
        self.assertEqual(len(rule_ids), len(set(rule_ids)), "Duplicate rule ID")
        self.assertEqual(len(case_ids), len(set(case_ids)), "Duplicate UC ID")
        mapped = set()
        for row in self.rows:
            cells = [cell.strip() for cell in row.strip("|").split("|")]
            with self.subTest(case=cells[0]):
                self.assertEqual(len(cells), 4)
                self.assertTrue(all(cells))
                refs = set(re.findall(r"\b(?!UC-)[A-Z]+-\d{3}\b", cells[0]))
                self.assertTrue(refs, "UC needs explicit rule IDs")
                mapped.update(refs)
        self.assertEqual(set(rule_ids), mapped, "Missing or unknown rule mapping")

    def test_states_questions_and_declared_total(self):
        questions = set(re.findall(r"^\| (Q-\d{2}) \|", self.rules, re.M))
        for row in self.rows:
            evidence = row.strip("|").split("|")[-1].strip()
            with self.subTest(case=row.split("<br>")[0]):
                self.assertRegex(evidence,
                                 r"^(\?|0|R|H) / (N|F|P|B\(Q-\d{2}(, Q-\d{2})*\)); .+")
                self.assertLessEqual(set(re.findall(r"Q-\d{2}", evidence)), questions)
        total = re.search(r"\*\*Všech (\d+) UC", self.cases)
        self.assertIsNotNone(total)
        self.assertEqual(int(total[1]), len(self.rows))


if __name__ == "__main__":
    unittest.main()
