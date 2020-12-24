# Usable-HTTP-Proxy

## What is a Proxy?
A Proxy allows a user to intercept and manipulate requests (sent through a browser) and responses (received through a server). For example, we can visit certain websites to see news that, fraudulently, obtain information from us (how often we visit the page, how much time we spend on it, etc.). Using the Proxy and configuring it in a very simple way, we will prevent this type of thing from happening.

## Introduction
Implementation of a Proxy, with configurable rules through a web interface deployed together with the Proxy itself.
The web interface offers two modes:
1. ** Basic mode **:
It allows users with or without knowledge to configure certain proxy parameters, without going into too technical details.
1. ** Advanced mode **:
Allows more advanced users to configure the Proxy.
Proxy works "in" HTTP and HTTPS mode.

## Objective
The purpose of the Proxy is to offer an easily configurable user interface through which both advanced and novice users can adjust the Proxy based on their preferences.

## Technologies used
* Java, JavaScript, CSS, HTML, XML, ...
* Spring Boot
* Socket (Client & Server)
* Tomcat (Integrated in Spring Boot framework)
* Bootstrap
* BouncyCastle (Used for the generation of CA Certificates and End-User Certificates, that is to say, when user connects to a website that implements HTTPS (HTTP+SSL).
* ... Less relevant technologies :)

CONTACT:
pablood9@hotmail.com
github.com/PablooD9
