#!/usr/bin/python3
from subprocess import check_output
import os
import json
import re

# get configuration
HERE = os.path.dirname(__file__) or "."
CHANGELOG_SOURCE = os.path.join(HERE, "..", "CHANGELOG.json")
CHANGELOG_OUTPUT = os.path.join(HERE, "../app/src/main/assets/changelog/en.html")
SKELETON_PATH = os.path.join(HERE, "changelog.html")

# define functions
def get_issue_links_from(commit_hash):
    body = check_output(["git", "log", "--pretty=%B", "-1", commit_hash])
    body = body.decode("UTF-8")
    return re.findall("(?:niccokunzmann/mundraub-android/issues?/|#)(\\d+)", body)

# get data
command = ["git", "log", "--date=short", "--pretty=%H %ad %an"]
commits = check_output(command)
commits = commits.decode("UTF-8")

with open(CHANGELOG_SOURCE, encoding="UTF-8") as f:
    changelog = json.load(f)

with open(SKELETON_PATH, encoding="UTF-8") as f:
    skeleton = f.read()

# convert content
content = ["<ul>"]
for commit in commits.split("\n"):
    if not commit:
        continue
    hash, date, author = commit.split(" ", 2)
    description = changelog.pop(hash, None)
    if not description:
        continue
    content.append("""
    <li>
                <a href="https://github.com/niccokunzmann/mundraub-android/commit/{hash}"
                   class="date">{date}</a> -
                <a class="author">{author}</a> -
                {issues}
                <p class="description">{description}</p>
            </li>
    """.strip("\n").format(
        hash=hash,
        date=".".join(reversed(date.split("-"))),
        author=author,
        description=description,
        issues=",\n                ".join([
            '<a href="https://github.com/niccokunzmann/mundraub-android/issues/{0}">Issue {0}</a>'
            .format(issue) for issue in get_issue_links_from(hash)
        ]),
    ))
    print(hash, date, author)

content.append("</ul>")

# check output
assert not changelog, "All descriptions in the changelog must be used."

# generate output
with open(CHANGELOG_OUTPUT, "w", encoding="UTF-8") as f:
    string_content = "\n        ".join(content)
    f.write(skeleton.format(content=string_content))

print("changelog written to " + os.path.abspath(CHANGELOG_OUTPUT))

