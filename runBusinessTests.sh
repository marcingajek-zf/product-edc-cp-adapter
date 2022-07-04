#!/bin/bash

export PLATO_DATA_MANAGEMENT_URL=$(minikube service plato-edc-controlplane -n edc-all-in-one --url | sed -n 3p)
export PLATO_DATA_MANAGEMENT_URL="${PLATO_DATA_MANAGEMENT_URL}/data"
export PLATO_IDS_URL=$(minikube service plato-edc-controlplane -n edc-all-in-one --url | sed -n 5p)
export PLATO_IDS_URL="${PLATO_IDS_URL}/api/v1/ids"
export PLATO_DATA_PLANE_URL=foo
export PLATO_DATA_MANAGEMENT_API_AUTH_KEY=password

export SOKRATES_DATA_MANAGEMENT_URL=$(minikube service sokrates-edc-controlplane -n edc-all-in-one --url | sed -n 3p)
export SOKRATES_DATA_MANAGEMENT_URL="${SOKRATES_DATA_MANAGEMENT_URL}/data"
export SOKRATES_IDS_URL=$(minikube service sokrates-edc-controlplane -n edc-all-in-one --url | sed -n 5p)
export SOKRATES_IDS_URL="${SOKRATES_IDS_URL}/api/v1/ids"
export SOKRATES_DATA_PLANE_URL=foo
export SOKRATES_DATA_MANAGEMENT_API_AUTH_KEY=password

./mvnw spotless:apply test -pl edc-tests
