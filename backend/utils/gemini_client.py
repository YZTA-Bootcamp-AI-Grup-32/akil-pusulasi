import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise ValueError("CRITICAL: GOOGLE_API_KEY environment variable not set.")
genai.configure(api_key=GOOGLE_API_KEY) 


model = genai.GenerativeModel('gemini-1.5-flash')

def construct_prompt(full_name: str, interests: list, last_game_score: int, journal_content: str) -> str:
    """
    This is the heart of your AI. It constructs the "Master Prompt".
    """
    interests_str = ", ".join(interests) if interests else "general topics"

    prompt = f"""
    You are 'Akıl Pusulası', a gentle, empathetic, and encouraging cognitive health assistant.
    Your user, {full_name}, has provided you with some information.
    Your goal is to provide a short, personalized, non-medical cognitive exercise or a motivational message based on their input.
    Keep your response to a maximum of 3-4 sentences. Be warm and supportive.

    User's Information:
    - Interests: {interests_str}
    - Recent game score: {last_game_score} out of a possible 10 (A higher score is better).
    - Today's journal entry: "{journal_content}"

    Based on all this information, generate a supportive response and a simple, related cognitive task.
    Do not give medical advice. Do not sound like a robot.
    """
    return prompt.strip()


async def generate_ai_response(prompt: str) -> str:
    """
    Sends the prompt to the Gemini API and returns the generated text.
    """
    try:
        response = await model.generate_content_async(prompt)
        
        if not response.parts:
            return "I'm having a little trouble thinking of a response right now. Let's try again later."
        return response.text
    except Exception as e:
        print(f"Error generating content from Gemini: {e}")
        return "I'm sorry, I couldn't generate a response at this moment. Please try again."