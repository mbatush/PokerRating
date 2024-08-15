## Create mongodb secret

```shell
kubectl -n poker create secret generic mongodb \
  --from-literal=username=root \
  --from-literal=password='<psswd_here>'
```

## Deploy helm
```shell
helm -n poker upgrade -i  rule-engine stef/rule-engine --version 1.0.1 \
  -f k8s/rule_engine/values.yaml
```
