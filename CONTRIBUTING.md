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

## Pull Requests

You can create a pull request in the following way.

### First time contribution

1. [fork] the repository
2. Your fork is present at https://github.com/YOURUSERNAME/mundraub-android.
    Replace `YOURUSERNAME` with your user name in the following.
3. Clone the fork
    ```bash
    git clone https://github.com/YOURUSERNAME/mundraub-android.git
    ```
    You can also use [ssh with GitHub], if you have generated a key.
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
