import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parents[1]))
import server


class RoutingTests(unittest.TestCase):
    def test_agent_catalog_matches_readme(self):
        names = {agent["name"] for agent in server.AGENTS}
        self.assertEqual(names, {
            "CENTRAL", "VIRUS_GUARD", "THREAT_ANALYST", "WEB_SCOUT",
            "CODE_GUARD", "FILE_GUARD", "ACTION_AGENT"
        })

    def test_routing(self):
        self.assertEqual(server.route_message("meu arquivo tem possível malware")["id"], "virus_guard")
        self.assertEqual(server.route_message("analise este phishing")["id"], "threat_analyst")
        self.assertEqual(server.route_message("revise o código do GitHub")["id"], "code_guard")
        self.assertEqual(server.route_message("pesquise este domínio")["id"], "web_scout")
        self.assertEqual(server.route_message("organize um workflow para bloquear")["id"], "action_agent")

    def test_actions_require_approval(self):
        self.assertTrue(server.needs_approval(server.agent_by_id("action_agent"), "isolar dispositivo"))
        self.assertTrue(server.needs_approval(server.agent_by_id("central"), "publicar relatório"))
        self.assertFalse(server.needs_approval(server.agent_by_id("code_guard"), "explique este bug"))

    def test_global_prefix_is_normalized(self):
        command, prefixed = server.normalize_command("• revise este código")
        self.assertEqual(command, "revise este código")
        self.assertTrue(prefixed)
        command, prefixed = server.normalize_command("pesquise este domínio")
        self.assertEqual(command, "pesquise este domínio")
        self.assertFalse(prefixed)


if __name__ == "__main__":
    unittest.main()
