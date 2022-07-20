/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
}

val rsApi: String by project

dependencies {
    api(project(":spi"))
    api(project(":core:contract"))
    api(project(":extensions:api:data-management:contractnegotiation"))
    api(project(":extensions:api:data-management:catalog-api"))
    api(project(":extensions:api:data-management:transferprocess"))
    api(project(":extensions:transaction:transaction-spi"))
    api(project(":extensions:api:api-core"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
}

publishing {
    publications {
        create<MavenPublication>("control-plane-adapter") {
            artifactId = "control-plane-adapter"
            from(components["java"])
        }
    }
}
