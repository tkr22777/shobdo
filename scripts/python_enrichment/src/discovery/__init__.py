# discovery — prefix-based lexical gap discovery pipeline
#
# Scripts:
#   generate_prefix_sets.py  — sort & chunk dictionary words into prefix sets
#   query_prefix_sets.py     — query an LLM per set to find missing words
#
# Run from scripts/python_enrichment/:
#   poetry run python src/discovery/generate_prefix_sets.py --source db
#   poetry run python src/discovery/query_prefix_sets.py --preview 2
