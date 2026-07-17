# Todo REST API

A simple Todo REST API built with Node.js + Express, storing data in memory.

## Quick Start

```bash
npm install
node index.js
```

Server starts at `http://localhost:3000`.

## API Endpoints

### GET /todos

List all todos.

```bash
curl http://localhost:3000/todos
```

### POST /todos

Create a new todo.

```bash
curl -X POST http://localhost:3000/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Buy groceries"}'
```

### PUT /todos/:id

Update a todo.

```bash
# Mark as completed
curl -X PUT http://localhost:3000/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'

# Change title
curl -X PUT http://localhost:3000/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Buy organic groceries"}'
```

### DELETE /todos/:id

Delete a todo.

```bash
curl -X DELETE http://localhost:3000/todos/1
```

## Data Shape

Each todo has these fields:

| Field     | Type    | Description                  |
|-----------|---------|------------------------------|
| id        | number  | Auto-incrementing identifier |
| title     | string  | Todo text                    |
| completed | boolean | Defaults to `false`          |
| createdAt | string  | ISO 8601 timestamp           |

## Error Responses

| Status | When                                    |
|--------|---------------------------------------- |
| 400    | Missing/invalid `title` or `completed`   |
| 404    | Todo with given `id` not found           |

## Environment Variables

| Variable | Default | Description          |
|----------|---------|----------------------|
| PORT     | 3000    | Server listening port |
