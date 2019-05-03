// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.server;

import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.events.zeromq.ZeroMqEventBus;
import org.openqa.selenium.grid.config.Config;
import org.zeromq.ZContext;

import java.util.Objects;
import java.util.logging.Logger;

public class EventBusConfig {

  public static final Logger LOG = Logger.getLogger(EventBus.class.getName());
  private final Config config;
  private EventBus bus;

  public EventBusConfig(Config config) {
    this.config = Objects.requireNonNull(config, "Config must be set.");
  }

  public EventBus getEventBus() {
    String publish = config.get("events", "publish")
        .orElseThrow(() -> new IllegalArgumentException(
            "Unable to determine event bus publishing connection string"));

    String subscribe = config.get("events", "subscribe")
        .orElseThrow(() -> new IllegalArgumentException(
            "Unable to determine event bus subscription connection string"));

    if (subscribe.equals(publish)) {
      throw new IllegalArgumentException(String.format(
          "Publish (%s) and subscribe (%s) connections must not be the same.",
          publish,
          subscribe));
    }

    boolean bind = config.getBool("events", "bind").orElse(false);

    if (bus == null) {
      synchronized (this) {
        if (bus == null) {
          LOG.finest("Creating new event bus");
          ZContext context = new ZContext();
          bus = ZeroMqEventBus.create(context, publish, subscribe, bind);
        }
      }
    }
    return bus;
  }
}
