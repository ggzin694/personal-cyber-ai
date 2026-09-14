import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parents[1]))
import server


class DefensiveFlowTests(unittest.TestCase):
    def test_quick_commands_are_local(self):
        for command in ("ajuda", "comandos", "menu", "agentes", "status", "verificar status"):
            self.assertIsNotNone(server.quick_reply(command))

    def test_link_and_file_guidance_is_local(self):
        self.assertIn("sem abrir", server.quick_reply("revisar link").lower())
        self.assertIn("sem executar", server.quick_reply("revisar arquivo").lower())

    def test_code_snippets_route_to_code_guard(self):
        self.assertEqual(server.route_message("def verificar_senha(token):\n    return token")['id'], "code_guard")
        self.assertEqual(server.route_message("console.log(password)")['id'], "code_guard")

    def test_external_action_confirmation_is_required(self):
        agent = server.agent_by_id("action_agent")
        self.assertTrue(server.needs_approval(agent, "publicar este relatório"))
        response = server.fallback_reply("publicar este relatório", agent, "http_429")
        self.assertIn("confirme o alvo", response.lower())


if __name__ == "__main__":
    unittest.main()
