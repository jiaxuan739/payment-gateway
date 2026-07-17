const express = require('express');

const app = express();
app.use(express.json());

// In-memory storage
const todos = [];
let nextId = 1;

// GET /todos — list all todos
app.get('/todos', (_req, res) => {
  res.json(todos);
});

// POST /todos — create a new todo
app.post('/todos', (req, res) => {
  const { title } = req.body;

  if (!title || typeof title !== 'string' || title.trim() === '') {
    return res.status(400).json({ error: 'title is required and must be a non-empty string' });
  }

  const todo = {
    id: nextId++,
    title: title.trim(),
    completed: false,
    createdAt: new Date().toISOString(),
  };

  todos.push(todo);
  res.status(201).json(todo);
});

// PUT /todos/:id — update a todo
app.put('/todos/:id', (req, res) => {
  const id = Number(req.params.id);

  if (isNaN(id)) {
    return res.status(400).json({ error: 'id must be a number' });
  }

  const todo = todos.find(t => t.id === id);
  if (!todo) {
    return res.status(404).json({ error: 'todo not found' });
  }

  const { title, completed } = req.body;

  if (title !== undefined) {
    if (typeof title !== 'string' || title.trim() === '') {
      return res.status(400).json({ error: 'title must be a non-empty string' });
    }
    todo.title = title.trim();
  }

  if (completed !== undefined) {
    if (typeof completed !== 'boolean') {
      return res.status(400).json({ error: 'completed must be a boolean' });
    }
    todo.completed = completed;
  }

  res.json(todo);
});

// DELETE /todos/:id — delete a todo
app.delete('/todos/:id', (req, res) => {
  const id = Number(req.params.id);

  if (isNaN(id)) {
    return res.status(400).json({ error: 'id must be a number' });
  }

  const index = todos.findIndex(t => t.id === id);
  if (index === -1) {
    return res.status(404).json({ error: 'todo not found' });
  }

  todos.splice(index, 1);
  res.status(204).send();
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
