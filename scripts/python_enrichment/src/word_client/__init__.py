"""
word_client — CRUD client for the Shobdo REST API.

    from src.word_client.client import WordClient

    client = WordClient("http://localhost:32779")
    word   = client.get_by_spelling("শব্দ")
    word   = client.create("নতুন")
    word   = client.add_meaning(word["id"], {"text": "something new", "partOfSpeech": "NOUN"})
"""
