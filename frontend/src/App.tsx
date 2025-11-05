import { useState } from 'react'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1>HideArea - Frontend</h1>
      <p>マイクロカーネルアーキテクチャ マルチデバイス対応Webサービスプラットフォーム</p>
      <div>
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
      </div>
      <p style={{ marginTop: '20px', color: '#666' }}>
        Edit <code>src/App.tsx</code> and save to test HMR
      </p>
    </div>
  )
}

export default App
