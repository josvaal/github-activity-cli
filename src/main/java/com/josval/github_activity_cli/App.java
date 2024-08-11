package com.josval.github_activity_cli;

import java.util.*;
import java.util.concurrent.Callable;

import com.josval.github_activity_cli.services.GithubService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "github-activity", mixinStandardHelpOptions = true, version = "github-activity 1.0", description = "Fetches and displays recent GitHub activities.")
public class App implements Callable<Integer> {
    GithubService githubService = new GithubService();

    @Parameters(index = "0", description = "GitHub username")
    private String username;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        if (username == null || username.isEmpty()) {
            System.out.println("Use a github username for the listed activities.");
            System.out.println("Example: github-activity <username>");
            return 0;
        }
        List<Map<String, Object>> events = githubService.ListEvents(username);

        if(events == null){
            System.out.println("[!] No events found or API call failed.");
            return 0;
        }

        Map<String, Integer> commitCounts = new HashMap<>();
        Map<String, Integer> eventCounts = new HashMap<>();
        Map<String, Set<String>> starredRepos = new HashMap<>();

        for (Map<String, Object> event: events) {
            String type = (String) event.get("type");
            Map<String, Object> repo = (Map<String, Object>) event.get("repo");
            String repoName = (String) repo.get("name");

            if ("PushEvent".equals(type)) {
                Map<String, Object> payload = (Map<String, Object>) event.get("payload");
                int commits = ((List<?>) payload.get("commits")).size();
                commitCounts.put(repoName, commitCounts.getOrDefault(repoName, 0) + commits);
            } else if ("IssuesEvent".equals(type) && "opened".equals(((Map<String, Object>) event.get("payload")).get("action"))) {
                eventCounts.put(repoName + " - Opened a new issue", eventCounts.getOrDefault(repoName + " - Opened a new issue", 0) + 1);
            } else if ("WatchEvent".equals(type)) {
                starredRepos.computeIfAbsent(repoName, k -> new HashSet<>()).add(username);
            } else {
                eventCounts.put(repoName + " - Event type " + type, eventCounts.getOrDefault(repoName + " - Event type " + type, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : commitCounts.entrySet()) {
            System.out.printf("- Pushed %d commits to %s%n", entry.getValue(), entry.getKey());
        }

        for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
            System.out.printf("- %s (%d times)%n", entry.getKey(), entry.getValue());
        }

        for (String repoName : starredRepos.keySet()) {
            System.out.printf("- Starred %s%n", repoName);
        }

        return 0;
    }
}

