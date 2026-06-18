# Helpdesk

## FastPing
```bash
# CreateTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"T","description":"D","requesterId":"u1"}'

# AssignTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets/<id>/assign

# EscalateTicketUC
wait for SLA deadline

# ResolveTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets/<id>/resolve \
  -H 'Content-Type: application/json' \
  -d '{"resolution":"Fixed"}'

# AddCommentUC
curl -X POST http://127.0.0.1:7070/api/tickets/<id>/comments \
  -H 'Content-Type: application/json' \
  -d '{"authorId":"u2","content":"Checked log files"}'

# GetTicketUC
curl http://127.0.0.1:7070/api/tickets/<id>
```
