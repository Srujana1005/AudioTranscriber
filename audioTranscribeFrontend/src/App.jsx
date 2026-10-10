
import { useRef, useState } from "react";
import "./App.css";

function App() {
  const [isRecording, setIsRecording] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [transcript, setTranscript] = useState("");
  const [error, setError] = useState("");

  const recorderRef = useRef(null);
  const chunksRef = useRef([]);

  const startRecording = async () => {
    try {
      setError("");
      setTranscript("");

      const stream = await navigator.mediaDevices.getUserMedia({
        audio: true,
      });

      chunksRef.current = [];

      const recorder = new MediaRecorder(stream);
      recorderRef.current = recorder;

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      recorder.onstop = async () => {
        stream.getTracks().forEach((track) => track.stop());

        const audioBlob = new Blob(chunksRef.current, {
          type: recorder.mimeType || "audio/webm",
        });

        if (audioBlob.size === 0) {
          setError("No audio recorded. Please try again.");
          return;
        }

        const formData = new FormData();
        formData.append("file", audioBlob, "recording.webm");

        setIsLoading(true);

        try {
          const response = await fetch(
            "http://localhost:8080/api/transcribe/transcribe",
            {
              method: "POST",
              body: formData,
            }
          );

          const result = await response.text();

          if (!response.ok) {
            throw new Error(result || "Transcription failed.");
          }

          setTranscript(result);
        } catch (err) {
          setError(err.message || "Could not transcribe the audio.");
        } finally {
          setIsLoading(false);
        }
      };

      recorder.start();
      setIsRecording(true);
    } catch (err) {
      setError(
        "Could not access your microphone. Please allow microphone permission."
      );
    }
  };

  const stopRecording = () => {
    if (recorderRef.current?.state === "recording") {
      setIsRecording(false);
      recorderRef.current.stop();
    }
  };

  return (
    <div className="app">
      <div className="card">
        <h1>Speech to Text</h1>
        <p className="subtitle">
          Speak naturally and convert your voice into text.
        </p>

        <div className="mic-icon">
          {isRecording ? "🔴" : "🎙️"}
        </div>

        <p className="status">
          {isRecording
            ? "Recording... Speak now!"
            : isLoading
            ? "Converting speech to text..."
            : "Ready to record"}
        </p>

        <div className="buttons">
          <button
            className="start-btn"
            onClick={startRecording}
            disabled={isRecording || isLoading}
          >
            Start Recording
          </button>

          <button
            className="stop-btn"
            onClick={stopRecording}
            disabled={!isRecording}
          >
            Stop & Transcribe
          </button>
        </div>

        {error && <p className="error">{error}</p>}

        <div className="result">
          <h2>Transcribed Text</h2>
          <p>
            {isLoading
              ? "Please wait while Groq processes your audio..."
              : transcript || "Your recognized words will appear here."}
          </p>
        </div>
      </div>
    </div>
  );
}

export default App;
