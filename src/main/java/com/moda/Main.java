package com.moda;

import com.moda.server.ModaServer;

/**
 * Точка входа в приложение.
 *
 * Запускает HTTP-сервер.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        new ModaServer().start();
    }
}
