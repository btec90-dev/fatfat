import express from 'express';
import cors from 'cors';

const app = express();
app.use(express.json());
app.use(cors());

app.post('/api/gemini', async (req, res) => {
  try {
    const { prompt, apiKey } = req.body;
    const key = apiKey || process.env.GEMINI_API_KEY;
    if (!key) {
      return res.status(400).json({ error: 'Gemini API key is missing. Please configure it in settings.' });
    }
    res.json({ reply: `Assistant Maman & Bébé : J'ai bien reçu votre question ("${prompt}"). Tout se déroule au mieux pour votre grossesse et votre protocole PMA. Prenez soin de vous !` });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`Secure server running on port ${PORT}`);
});
