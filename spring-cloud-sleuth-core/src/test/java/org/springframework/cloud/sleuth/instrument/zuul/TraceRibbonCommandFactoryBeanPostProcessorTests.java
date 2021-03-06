/*
 * Copyright 2013-2018 the original author or authors.
 *
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
 */

package org.springframework.cloud.sleuth.instrument.zuul;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.sleuth.ExceptionMessageErrorParser;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.instrument.web.SleuthHttpParserAccessor;
import org.springframework.cloud.sleuth.util.ArrayListSpanReporter;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(MockitoJUnitRunner.class)
public class TraceRibbonCommandFactoryBeanPostProcessorTests {

	ArrayListSpanReporter reporter = new ArrayListSpanReporter();
	Tracing tracing = Tracing.newBuilder()
			.currentTraceContext(CurrentTraceContext.Default.create())
			.spanReporter(this.reporter)
			.build();
	TraceKeys traceKeys = new TraceKeys();
	HttpTracing httpTracing = HttpTracing.newBuilder(this.tracing)
			.clientParser(SleuthHttpParserAccessor.getClient(this.traceKeys))
			.serverParser(SleuthHttpParserAccessor.getServer(this.traceKeys, new ExceptionMessageErrorParser()))
			.build();

	@Mock RibbonCommandFactory ribbonCommandFactory;
	@Mock BeanFactory beanFactory;
	@InjectMocks TraceRibbonCommandFactoryBeanPostProcessor postProcessor;

	@Test
	public void should_return_a_bean_as_it_is_if_its_not_a_ribbon_command_Factory() {
		then(this.postProcessor.postProcessAfterInitialization("", "name")).isEqualTo("");
	}

	@Test
	public void should_wrap_ribbon_command_factory_in_a_trace_representation() {
		then(this.postProcessor.postProcessAfterInitialization(ribbonCommandFactory, "name")).isInstanceOf(
				TraceRibbonCommandFactory.class);
	}

	@Before
	public void setup() {
		BDDMockito.given(this.beanFactory.getBean(HttpTracing.class)).willReturn(this.httpTracing);
	}
}