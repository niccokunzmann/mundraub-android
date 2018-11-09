# Contributions

## Commits
[commits]: #commits

Please have a look at the commit history for examples.
```
first line describes in present tense what the commit does

Problem: If it is not described in the first line
Solution: If it is not described in the first line

- related/fixes/contributes to link to issue
- changes in this commit i.e. bug fix/refactoring
```

Commits referencing issues will turn up in the [release history].

Examples chosen from the [commits]:
- [fix broken map link](https://github.com/niccokunzmann/mundraub-android/commit/8506ed683d779b68af7757c1a51323d4ec5037e5)
- [Update development process](https://github.com/niccokunzmann/mundraub-android/commit/602c29079e802f6ad446899f029f33d6b6b15fa5)
- [remove duplicate edit button](https://github.com/niccokunzmann/mundraub-android/commit/09f95acbdca137341c4eaf8bd052357921c7d490)

## Replacing a commit

If you have made a commit you would like to improve and it is the last one on the branch, use

    git reset HEAD~

to keep the files but remove the commit.

    git add ...
    git commit ...
    git push -f ...

will then create a new commit with a new message and push it to your branch.

## Pull Requests

You can create a pull request by following the steps below.

### First time contribution

1. [fork] the repository
2. Your fork is present at https://github.com/YOURUSERNAME/mundraub-android.
    Replace `YOURUSERNAME` with your user name in the following.
3. Clone the fork
    ```bash
    git clone https://github.com/YOURUSERNAME/mundraub-android.git
    ```
    Alternatively, use [ssh with GitHub], if you have generated a key.
    ```bash
    git clone git@github.com:YOURUSERNAME/mundraub-android.git
    ```
5. Create a new branch for the contribution. Replace `BRANCHNAME` with a suitable name without spaces in the following.
    ```bash
    git checkout -b BRANCHNAME
    ```
6. Do your changes.
7. Create a commit using the [commit format][commits] described above.
    ```bash
    git commit -m"...
    ...
    ..."
    ```
8. Push the commit.
    ```bash
    git push -u origin BRANCHNAME
    ```
9. Create a pull request. Replace `YOURUSERNAME` and `BRANCHNAME` in this url
    https://github.com/niccokunzmann/mundraub-android/compare/master...YOURUSERNAME:BRANCHNAME
    or configure it with `compare across forks`:
    https://github.com/niccokunzmann/mundraub-android/compare
10. Describe the pull request. Reference the issue.
11. Create the pull request. We will review it!



[fork]: https://github.com/niccokunzmann/mundraub-android/fork
[ssh with GitHub]: https://help.github.com/articles/connecting-to-github-with-ssh/
[release history]: https://github.com/niccokunzmann/mundraub-android/releases
[commits]: https://github.com/niccokunzmann/mundraub-android/commits/master
