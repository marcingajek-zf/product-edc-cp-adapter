# Invoke Business-Tests via Maven

```shell
./mvnw -pl edc-tests test -Dtest="RunCucumberTest"
```

# Test locally using Act Tool

> "Think globally, [`act`](https://github.com/nektos/act) locally"

```shell
act -j business-test
```