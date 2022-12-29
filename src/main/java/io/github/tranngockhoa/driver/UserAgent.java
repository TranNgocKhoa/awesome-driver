package io.github.tranngockhoa.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserAgent {
    private final String AGENT_LIST_URL = "https://raw.githubusercontent.com/Kikobeats/top-user-agents/f3641632b36530aa49d7e51c6a7dff0c3798d229/index.json";

    private final Random random = new Random();

    private static List<String> AGENT_LIST;

    public UserAgent() {
        if (AGENT_LIST == null || AGENT_LIST.isEmpty()) {
            AGENT_LIST = getUserAgentList();
        }
    }

    public String getRandomAgent() {
        return AGENT_LIST.get(random.nextInt(AGENT_LIST.size()));
    }

    public List<String> getUserAgentList() {
        try {
            URL url = new URL(AGENT_LIST_URL);

            try (InputStream gitAgentDataStream = url.openStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(gitAgentDataStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line;
                List<String> agentList = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (!line.equals("[") && !line.equals("]")) {
                        String agent = line.trim().replaceFirst("\"", "").replaceFirst("\",", "");
                        if (!agent.contains("X11")) {
                            agentList.add(agent);
                        }
                    }
                }

                return agentList;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
