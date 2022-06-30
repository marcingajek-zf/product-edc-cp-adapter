#!/bin/bash

export PLATO_DATA_MANAGEMENT_URL=$(minikube service plato-edc-controlplane -n edc-all-in-one --url | sed -n 3p)
echo "PLATO_DATA_MANAGEMENT_URL: ${PLATO_DATA_MANAGEMENT_URL}"

export PLATO_IDS_URL=$(minikube service plato-edc-controlplane -n edc-all-in-one --url | sed -n 5p)
echo "PLATO_IDS_URL: ${PLATO_IDS_URL}"

export PLATO_DATA_PLANE_URL=foo
echo "PLATO_DATA_PLANE_URL: ${PLATO_DATA_PLANE_URL}"

export PLATO_DATA_MANAGEMENT_API_AUTH_KEY=password
echo "PLATO_DATA_MANAGEMENT_API_AUTH_KEY: ${PLATO_DATA_MANAGEMENT_API_AUTH_KEY}"

export SOKRATES_DATA_MANAGEMENT_URL=$(minikube service sokrates-edc-controlplane -n edc-all-in-one --url | sed -n 3p)
echo "SOKRATES_DATA_MANAGEMENT_URL: ${SOKRATES_DATA_MANAGEMENT_URL}"

export SOKRATES_IDS_URL=$(minikube service sokrates-edc-controlplane -n edc-all-in-one --url | sed -n 5p)
echo "SOKRATES_IDS_URL: ${SOKRATES_IDS_URL}"

export SOKRATES_DATA_PLANE_URL=foo
echo "SOKRATES_DATA_PLANE_URL: ${SOKRATES_DATA_PLANE_URL}"

export SOKRATES_DATA_MANAGEMENT_API_AUTH_KEY=password
echo "SOKRATES_DATA_MANAGEMENT_API_AUTH_KEY: ${SOKRATES_DATA_MANAGEMENT_API_AUTH_KEY}"

./mvnw spotless:apply test -pl edc-tests \
    -DPLATO_DATA_MANAGEMENT_URL=${PLATO_DATA_MANAGEMENT_URL} \
    -DPLATO_IDS_URL=${PLATO_IDS_URL} \
    -DPLATO_DATA_PLANE_URL=${PLATO_DATA_PLANE_URL} \
    -DSOKRATES_DATA_MANAGEMENT_URL=${SOKRATES_DATA_MANAGEMENT_URL} \
    -DSOKRATES_IDS_URL=${SOKRATES_IDS_URL} \
    -DSOKRATES_DATA_PLANE_URL=${SOKRATES_DATA_PLANE_URL}
