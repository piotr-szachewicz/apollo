/*
 * -\-\-
 * Spotify Apollo HTTP Service
 * --
 * Copyright (C) 2013 - 2015 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */
package com.spotify.apollo.httpservice;

import com.google.inject.TypeLiteral;

import com.spotify.apollo.AppInit;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.environment.ApolloEnvironment;
import com.spotify.apollo.environment.ApolloEnvironmentModule;
import com.spotify.apollo.environment.ClientDecorator;
import com.spotify.apollo.environment.ListBasedComparator;
import com.spotify.apollo.meta.MetaModule;
import com.spotify.apollo.module.AbstractApolloModule;
import com.spotify.apollo.request.RequestHandler;

import java.util.Comparator;

import static java.util.Collections.singletonList;

/**
 * Used to gather dependencies from other modules into {@link HttpService}
 */
class HttpServiceModule extends AbstractApolloModule {

  private final AppInit appInit;

  private HttpServiceModule(AppInit appInit) {
    this.appInit = appInit;
  }

  public static HttpServiceModule create(AppInit appInit) {
    return new HttpServiceModule(appInit);
  }

  @Override
  protected void configure() {
    bindAppInit();
    bindClientDecoratorComparator();

    install(ApolloEnvironmentModule.create());
    install(new MetaModule("apollo-http"));
  }

  private void bindAppInit() {
    bind(Initializer.class).toInstance(env -> env.initialize(appInit));
  }

  private void bindClientDecoratorComparator() {
    // ensure that clients track outgoing calls first in the chain
    bind(new TypeLiteral<Comparator<ClientDecorator.Id>>() { })
        .toInstance(new ListBasedComparator(singletonList(MetaModule.OUTGOING_CALLS))
    );
  }

  static RequestHandler requestHandler(Service.Instance instance) {
    final ApolloEnvironment environment = ApolloEnvironmentModule.environment(instance);
    final Initializer initializer = instance.resolve(Initializer.class);
    return initializer.init(environment);
  }

  @Override
  public String getId() {
    return "apollo-http-service";
  }

  private interface Initializer {
    RequestHandler init(ApolloEnvironment apolloEnvironment);
  }
}
