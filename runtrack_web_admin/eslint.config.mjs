export default [
  {
    files: ["**/*.js", "**/*.mjs", "**/*.ts"],
    rules: {
      "no-unused-vars": "error",
      "no-undef": "error",
      "complexity": ["error", 10],
      "max-lines-per-function": ["error", { "max": 50, "skipBlankLines": true, "skipComments": true }],
      "max-lines": ["error", { "max": 350, "skipBlankLines": true, "skipComments": true }],
      "no-restricted-imports": ["error", {
        "patterns": [{ "group": ["**/ui/*"], "message": "Module trong core/ không được phép import từ ui/." }]
      }]
    }
  }
];
