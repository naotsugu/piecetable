/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor;

import com.mammb.code.editor.fx.App;
import java.util.Locale;

/**
 * The application entry point.
 * @author Naotsugu Kobayashi
 */
public class Main {

    /**
     * Launch application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // output logs and button names in english
        Locale.setDefault(Locale.US);
        System.setProperty("user.country","US");
        System.setProperty("user.language","en");

        // setup log format
        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");

        App.launch(App.class, args);
    }

}
