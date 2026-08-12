import js from '@eslint/js';
import prettierPlugin from 'eslint-plugin-prettier';
import simpleImportSortPlugin from 'eslint-plugin-simple-import-sort';
import tseslint from 'typescript-eslint';

export default tseslint.config(
	// register all of the plugins up-front
	{
		plugins: {
			['@typescript-eslint']: tseslint.plugin,
			['simple-import-sort']: simpleImportSortPlugin,
			['prettier']: prettierPlugin,
		},
	},

	// config with just ignores is the replacement for `.eslintignore`
	{
		ignores: ['dist', 'pnpm-lock.yaml', 'package.json'],
	},

	// extends ...
	js.configs.recommended,
	...tseslint.configs.recommended,

	// base config
	{
		rules: {
			'@typescript-eslint/consistent-type-assertions': [
				'warn',
				{
					assertionStyle: 'never',
				},
			],
			'@typescript-eslint/no-explicit-any': 'off',
			'@typescript-eslint/no-unused-vars': [
				'warn',
				{
					argsIgnorePattern: '^_',
				},
			],
			'simple-import-sort/imports': 'error',
			'simple-import-sort/exports': 'error',
			'prettier/prettier': 'error',
			curly: ['warn', 'all'],
		},
		languageOptions: {
			parserOptions: {
				ecmaVersion: 'latest',
				sourceType: 'module',
			},
		},
	},
);
