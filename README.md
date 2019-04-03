# Usable-HTTP-Proxy

## ¿Qué es un Proxy?
Un Proxy permite a un usuario interceptar y manipular las peticiones (enviadas a través de un navegador) y las respuestas (recibidas a través de un servidor). Por ejemplo, podemos visitar ciertos sitios web para ver noticias que, de forma fraudulenta, obtienen información nuestra (con qué frecuencia visitamos la página, cuánto tiempo pasamos en ella, etc). Usando el Proxy y configurándolo de una forma muy sencilla, evitaremos que sucedan este tipo de cosas.

## Introducción
Implementación de un Proxy, con reglas configurables a través de una interfaz web desplegada junto con el propio Proxy.
La interfaz web ofrece dos modalidades:
1. **Modalidad básica**:
Permite a usuarios con o sin conocimientos configurar ciertos parámetros del Proxy, sin entrar en detalles demasiado técnicos.
1. **Modalidad avanzada**:
Permite a los usuarios más avanzados configurar el Proxy.

## Objetivo
El objetivo del Proxy es ofrecer una interfaz de usuario fácilmente configurable a través de la cual, tanto usuarios avanzados como novatos, podrán ajustar el Proxy en base a sus preferencias.

## Tecnologías usadas
* Java
* Spring Boot
* Socket (Servidor y Cliente)
* Tomcat (integrado con el framework de Spring Boot)
* Bootstrap
