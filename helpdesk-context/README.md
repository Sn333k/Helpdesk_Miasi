# Helpdesk

## FastPing
```bash
# CreateTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets -d '{"title":"T","description":"D","requesterId":"u1"}'

# AssignTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets/<id>/assign

# EscalateTicketUC
wait for SLA deadline

# ResolveTicketUC
curl -X POST http://127.0.0.1:7070/api/tickets/{id}/resolve -d '{"resolution":"Fixed"}'

```
