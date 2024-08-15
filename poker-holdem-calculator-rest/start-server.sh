#!/bin/bash
gunicorn --worker-class "${WORKER_CLASS:-sync}" \
  --keep-alive "${KEEP_ALIVE:-30}" \
  -w "${WORKER:-4}" \
  -b 0.0.0.0:"${PORT:-8080}" \
  -t "${WORKER_TIMEOUT:-120}" \
  holdem_calc_rest:app
