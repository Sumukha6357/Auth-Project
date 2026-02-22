import js from "@eslint/js";
import next from "eslint-config-next";
import prettier from "eslint-config-prettier";

export default [
  {
    ignores: ["node_modules/**", "node_modules_broken/**", ".next/**"],
  },
  js.configs.recommended,
  ...next,
  prettier,
  {
    rules: {
      "no-console": ["warn", { allow: ["warn", "error"] }],
      "no-undef": "off",
      "no-unused-vars": "off",
      "react-hooks/set-state-in-effect": "off",
    },
  },
];
