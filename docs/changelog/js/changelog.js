
var REPO = "niccokunzmann/mundraub-android";

// API: "https://api.github.com/repos/niccokunzmann/mundraub-android/contents/CHANGELOG.json"
var CHANGELOG_GITHUB_URL = "https://raw.githubusercontent.com/" + REPO + "/master/CHANGELOG.json";
var CHANGELOG_URL = CHANGELOG_GITHUB_URL;

var requestsSent = 0;
function trackRequest(url, onSuccess, onFailure) {
    if (typeof(Storage) !== "undefined") {
        // Code for localStorage/sessionStorage.
        // see https://www.w3schools.com/html/html5_webstorage.asp
        var key = "url-" + url;
        var cachedValue = localStorage.getItem(key);
        if (cachedValue) {
            console.log("loaded " + key);
            onSuccess(JSON.parse(cachedValue));
        } else {
            sendRequest(url, function(json) {
                localStorage.setItem(key, JSON.stringify(json));
                console.log("stored " + key);
                onSuccess(json);
            }, onFailure);
            requestsSent++;
            console.log("request " + requestsSent + "\t" + url);
        }
    } else {
        // Sorry! No Web Storage support..
        sendRequest(url, onSuccess, onFailure);
        requestsSent++;
        console.log("request " + requestsSent + "\t" + url);
    }
    
}

var tagAllCommitsCalled = false;

// add the tags to all the commits
// count down from latest commit
function tagAllCommits() {
    if (tagAllCommitsCalled) {
        throw new Error("should only be called once!");
    }
    tagAllCommitsCalled = true;
    var url = "https://api.github.com/repos/" + REPO + "/releases/latest";
    trackRequest(url, function(json) {
        var tagArray = json.tag_name.match("^v(\\d+)\\.(\\d+)$"); // v1.3 -> ["v1.3", "1", "3"]
        if (!tagArray) {
            return;
        }
        var tagMajor = tagArray[1];
        function compareTag(commitElementIndex, tagMinor) {
            if (commitElementIndex >= commitElements.length || tagMinor <= 0) {
                return;
            }
            // compare tag to commit
            var element = commitElements[commitElementIndex];
            var sha = element.sha;
            var tag = "v" + tagMajor + "." + tagMinor;
            var url = "https://api.github.com/repos/" + REPO + "/compare/" + tag + "..." + sha;
            trackRequest(url, function(json) {
                if (json.status == "diverged" || json.status == "ahead") {
                    // If the value of the status attribute in the response is diverged or ahead, then the commit is not contained in the tag.
                    // see https://stackoverflow.com/a/37126420
                    compareTag(commitElementIndex + 1, tagMinor + 1);
                } else {
                    element.setTagName(tag);
                    compareTag(commitElementIndex, tagMinor - 1);
                }
            }, function() {
                compareTag(commitElementIndex, tagMinor - 1);
            });
        };
        compareTag(0, parseInt(tagArray[2]));
    }, function () {
        textLoading.innerText = "Failed latest tag.";
    })
}

var commitElements = [];

function addCommit(api, text) {
    // api, see https://developer.github.com/v3/repos/commits/#response
    var root = document.createElement("li");
    root.className = "change";
    root.id = api.sha;

    var tagName = document.createElement("a");
    
    var dateString = api.commit.author.date.substring(0, 10);
    var date = document.createElement("a");
    date.className = "date";
    date.innerText = dateString; // 2019-09-13
    date.href = api.html_url;
    
    var author = document.createElement("span");
    author.classList = "author";
    author.innerText = api.commit.author.name;
    
    var description = document.createElement("p");
    description.className = "description";
    description.innerHTML = text;
    
    var issueLinks = document.createElement("span");
    issueLinks.className = "issueLinks";
    var issues = api.commit.message.matchAll("(?:" + REPO + "/issues?/|#)(\\d+)");
    for (var issue = issues.next(); !issue.done; issue = issues.next()) {
        var issueId = issue.value[1];
        var issueLink = document.createElement("a");
        issueLink.innerText = "Issue " + issueId;
        issueLink.href = "https://github.com/" + REPO + "/issues/" + issueId;
        issueLink.className = "issueLink";
        issueLinks.appendChild(issueLink);
    }
    
    root.appendChild(tagName);
    root.appendChild(date);
    root.appendChild(author);
    root.appendChild(issueLinks);
    root.appendChild(description);
    
    root.sortId = dateString + api.sha;
    root.sha = api.sha;
    root.setTagName = function(tag) {
        tagName.className = "tag";
        tagName.innerText = tag;
        tagName.href = "https://github.com/" + REPO + "/releases/tag/" + tag;
    }
    commitElements.push(root);
}

function showCommitsWhenReady() {
    //console.log("NUMBER_OF_COMMITS(" + NUMBER_OF_COMMITS + ") == (" + commitElements.length + ")commitElements.length");
    if (NUMBER_OF_COMMITS == commitElements.length) {
        // sort by date
        // see https://stackoverflow.com/a/5002924
        commitElements.sort(function(a, b) {
            if (a.sortId < b.sortId) {
                return 1;
            } else if (a.sortId > b.sortId) {
                return -1;
            } else {
                return 0;
            }
        });
        commitElements.forEach(function(element) {
            allCommits.appendChild(element);
        });
        tagAllCommits();
    }
}

function loadCommit(commit, message) {
    var commitUrl = "https://api.github.com/repos/" + REPO + "/commits/" + commit;
    trackRequest(commitUrl, function(json) {
        addCommit(json, message);
        showCommitsWhenReady();
    }, function(event) {
        textLoading.innerText = "Failed to load commit " + commit + ".";
        NUMBER_OF_COMMITS--;
        showCommitsWhenReady();
    });
}

var NUMBER_OF_COMMITS = 0;
function onLoad() {
    textLoading.innerText = "Loading changelog ...";
    trackRequest(CHANGELOG_URL, function(json) {
        textLoading.innerText = "Loading commits ...";
        for (var commit in json) {
            // iterate commits
            // see https://stackoverflow.com/a/16735184
            if (Object.prototype.hasOwnProperty.call(json, commit)) {
                loadCommit(commit, json[commit]);
                NUMBER_OF_COMMITS++;
            }
        }
        showCommitsWhenReady();
        textLoading.innerText = "";
    }, function(event) {
        textLoading.innerText = "Error!";
    });
}

window.addEventListener("load", onLoad);

