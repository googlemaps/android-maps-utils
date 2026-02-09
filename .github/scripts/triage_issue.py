import os
import json
import urllib.request
import sys

def get_gemini_response(api_key, prompt):
# Using the stable Gemini 2.5 Flash
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
    headers = {'Content-Type': 'application/json'}
    data = {
        "contents": [{
            "parts": [{"text": prompt}]
        }],
        "generationConfig": {
            "response_mime_type": "application/json"
        }
    }
    
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res_data = json.loads(response.read().decode('utf-8'))
            return res_data['candidates'][0]['content']['parts'][0]['text']
    except urllib.error.HTTPError as e:
        print(f"Gemini API Error ({e.code}): {e.reason}", file=sys.stderr)
        try:
            error_body = e.read().decode('utf-8')
            print(f"Error details: {error_body}", file=sys.stderr)
        except:
            pass
        return None
    except Exception as e:
        print(f"Error calling Gemini API: {e}", file=sys.stderr)
        return None

def main():
    api_key = os.getenv("GEMINI_API_KEY")
    issue_title = os.getenv("ISSUE_TITLE")
    issue_body = os.getenv("ISSUE_BODY")

    if not api_key:
        print("GEMINI_API_KEY not found", file=sys.stderr)
        sys.exit(1)
        
    if not issue_title and not issue_body:
        print("Error: ISSUE_TITLE and ISSUE_BODY are both empty. Triage skipped.", file=sys.stderr)
        sys.exit(0) # Exit gracefully so the workflow doesn't just fail without a reason

    prompt = f"""
    You are an expert software engineer and triage assistant. 
    Analyze the following GitHub Issue details and suggest appropriate labels.
    
    Issue Title: {issue_title}
    Issue Description: {issue_body}
    
    Triage Criteria:
    - Severity:
        - priority: p0: Critical issues, crashes, security vulnerabilities (specifically if it mentions "crash" or "exception").
        - priority: p1: Important issues that block release.
        - priority: p2: Normal priority bugs or improvements.
        - priority: p3: Minor enhancements or non-critical fixes.
        - priority: p4: Low priority, nice-to-have eventually.

    Return a JSON object with a 'labels' key containing an array of suggested label names.
    The response MUST be valid JSON.
    Example: {{"labels": ["priority: p2", "type: bug"]}}
    """

    response_text = get_gemini_response(api_key, prompt)
    if response_text:
        try:
            # Clean up response text in case it has markdown wrapping
            if response_text.startswith("```json"):
                response_text = response_text.replace("```json", "", 1).replace("```", "", 1).strip()
            
            result = json.loads(response_text)
            labels = result.get("labels", [])
            # Print labels as a comma-separated string for GitHub Actions
            print(",".join(labels))
        except Exception as e:
            print(f"Error parsing Gemini response: {e}", file=sys.stderr)
            print(f"Raw response: {response_text}", file=sys.stderr)
            sys.exit(1)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()