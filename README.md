# Resume ATS Checker — Spring Boot + Hugging Face AI

Ek Spring Boot backend jo resume aur job description ko compare karke
ATS-style **match score**, **matched/missing keywords**, aur **suggestions** deta hai.

## Kaise kaam karta hai

1. `POST /api/ats/analyze` — resume file (`pdf`/`docx`/`txt`) + job description text leta hai.
2. `ResumeParserService` — file se plain text extract karta hai (PDFBox / Apache POI).
3. `HuggingFaceService` — Hugging Face **Inference API** ko call karta hai:
   - Model: `sentence-transformers/all-MiniLM-L6-v2`
   - Pipeline: `sentence-similarity`
   - Ye seedha 0–1 ke beech semantic similarity score deta hai (resume vs JD).
4. `KeywordExtractorService` — JD se important keywords nikaal kar resume me
   present/missing check karta hai (rule-based, free — koi extra API call nahi).
5. Response me match %, matched keywords, missing keywords, verdict aur
   improvement suggestions milte hain.
6. `static/index.html` — browser se test karne ke liye simple UI.

## Setup

### 1. Hugging Face token lo
- https://huggingface.co/settings/tokens par jaake ek **Access Token** (read scope) generate karo.

### 2. Environment variable set karo
```bash
export HF_API_KEY=hf_xxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 3. Run karo
```bash
cd resume-ats
mvn spring-boot:run
```

App start hoga: `http://localhost:8080`

### 4. Test karo
Browser me `http://localhost:8080` kholo — form se resume upload karo aur JD paste karo.

Ya curl se:
```bash
curl -X POST http://localhost:8080/api/ats/analyze \
  -F "resume=@/path/to/resume.pdf" \
  -F "jobDescription=We are looking for a Java Spring Boot developer with REST API and AWS experience..."
```

## Sample response
```json
{
  "matchScorePercent": 76.4,
  "matchedKeywords": ["java", "spring", "rest", "api"],
  "missingKeywords": ["aws", "docker", "kubernetes"],
  "verdict": "Good match — kuch improvements se aur behtar ho sakta hai.",
  "suggestions": [
    "In keywords ko resume me (agar genuinely applicable ho) natural tareeke se add karein: aws, docker, kubernetes.",
    "Bullet points ko job description ki language se match karein — recruiters aur ATS dono isse pasand karte hain.",
    "Quantifiable achievements (numbers, %, metrics) add karein taaki resume aur strong lage."
  ]
}
```

## Extend karne ke ideas
- **NER-based keyword extraction**: rule-based extractor ki jagah Hugging Face
  ka `dslim/bert-base-NER` ya `ml6team/keyphrase-extraction-kbir-inspec` model use karo — better skill detection.
- **AI-generated suggestions**: rule-based suggestions ki jagah ek text-generation
  model (jaise `mistralai/Mistral-7B-Instruct-v0.3`) ko prompt karke personalized,
  natural-language resume improvement tips generate karwao.
- **Resume rewriting**: missing keywords ke basis par AI se resume bullet points
  khud generate karwao.
- **Frontend**: React/Angular se replace karo static HTML ko, aur multiple resumes
  ko ek saath rank karne ka feature add karo (recruiter side ke liye).
- **Persistence**: results ko DB (Postgres/MongoDB) me save karo history ke liye.

## Deploy on Render

This project ships with a `Dockerfile` and `render.yaml`, so deployment is straightforward.

### Option A — One-click via Blueprint (recommended)
1. Push this project to a **GitHub repo** (Render deploys from Git, not a zip upload).
2. Go to https://dashboard.render.com → **New** → **Blueprint**.
3. Connect your GitHub repo. Render will detect `render.yaml` automatically.
4. When prompted, set the `HF_API_KEY` environment variable to your Hugging Face token.
5. Click **Apply** — Render builds the Docker image and deploys it.

### Option B — Manual Web Service
1. Push this project to GitHub.
2. Go to https://dashboard.render.com → **New** → **Web Service**.
3. Connect your repo.
4. Environment: **Docker** (Render auto-detects the `Dockerfile`).
5. Under **Environment Variables**, add:
   - `HF_API_KEY` = your Hugging Face token
6. Instance type: **Free** is fine to start.
7. Click **Create Web Service**.

### After deploying
- Render gives you a public URL like `https://resume-ats-xxxx.onrender.com`.
- Open it in a browser — the same UI you tested locally will load.
- `GET /api/ats/health` is wired up as Render's health check endpoint.
- **Free tier note**: the free instance spins down after inactivity, so the first request after idle time can take 30–60s to wake up — this is normal, not a bug.
- Never commit your `HF_API_KEY` into the repo — always set it as an environment variable in Render's dashboard.

## Notes
- **Important**: Hugging Face ne purana `api-inference.huggingface.co` endpoint deprecate kar diya hai (late 2025).
  Ye project ab naya endpoint use karta hai: `https://router.huggingface.co/hf-inference/models/{model}`.
- Hugging Face free Inference API ka pehla call slow ho sakta hai (model "cold start" ~15-20s) —
  code me `wait_for_model: true` already set hai isliye request fail nahi hogi, bas thoda wait karegi.
- File size limit 5MB rakhi gayi hai (`application.yml` me change kar sakte ho).
