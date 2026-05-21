import os
import re
from gitlint.rules import CommitRule, RuleViolation


class RequiresJiraFooter(CommitRule):
    """Enforce that each commit contains a 'Closes: MODLOC-XXX' footer line."""

    name = "body-requires-jira-footer"
    id = "UC1"

    FOOTER_PATTERN = re.compile(r"^Closes:\s+MODLOC-\d+$")

    def validate(self, commit):
        for line in commit.message.body:
            if self.FOOTER_PATTERN.match(line.strip()):
                return

        return [RuleViolation(
            self.id,
            "Commit message must contain a 'Closes: MODLOC-<number>' footer line",
            line_nr=1
        )]


class ValidConventionalCommitScope(CommitRule):
    """Enforce that a conventional commit scope, if specified, matches a feature in docs/features/."""

    name = "title-valid-conventional-commit-scope"
    id = "UC2"

    SCOPE_PATTERN = re.compile(r"^\w+\(([^)]+)\):")

    def _available_scopes(self):
        features_dir = os.path.join(
            os.path.dirname(__file__), "..", "..", "docs", "features"
        )
        features_dir = os.path.normpath(features_dir)
        if not os.path.isdir(features_dir):
            return []
        return [
            os.path.splitext(f)[0]
            for f in os.listdir(features_dir)
            if f.endswith(".md")
        ]

    def validate(self, commit):
        subject = commit.message.title
        match = self.SCOPE_PATTERN.match(subject)
        if not match:
            return

        scope = match.group(1)
        available = self._available_scopes()

        if scope not in available:
            return [RuleViolation(
                self.id,
                f"Scope '{scope}' is not a known feature. "
                f"Valid scopes: {', '.join(sorted(available))}",
                line_nr=1
            )]

