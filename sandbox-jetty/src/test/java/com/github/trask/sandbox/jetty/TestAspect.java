/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.trask.sandbox.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Trask Stalnaker
 */
@Aspect
public class TestAspect {

    @Pointcut("execution(void com.github.trask.sandbox.jetty.TestServlet.doGet(" +
            "javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse))")
    void doGetPointcut() {}

    @Around("doGetPointcut() && args(request, response)")
    public void aroundTopLevelServletPointcut(ProceedingJoinPoint joinPoint,
            HttpServletRequest request, HttpServletResponse response) throws Throwable {

        joinPoint.proceed();
        response.getWriter().print("/aspect-was-here");
    }
}
