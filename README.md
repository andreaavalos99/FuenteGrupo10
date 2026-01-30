Fuente Service

Servicio backend desarrollado en Java con Spring Boot.
Expone una API REST para la gestión de colecciones y hechos, pensada para funcionar como fuente de datos dentro de una arquitectura de microservicios.

El proyecto no incluye interfaz gráfica: su objetivo principal es el diseño de la API, el modelado del dominio y la integración con otros servicios.

Descripción
Este servicio permite:
Crear y consultar colecciones
Registrar hechos asociados a una colección
Exponer información a otros servicios mediante endpoints REST
Forma parte de un ecosistema mayor, donde otros componentes consumen esta información para tareas de búsqueda, procesamiento o visualización.

Arquitectura
El proyecto sigue una arquitectura en capas:
Controllers: exposición de endpoints REST
Fachada: centraliza la lógica del dominio
Dominio: entidades y reglas de negocio

Repositorios: acceso a datos
Se aplican patrones y conceptos comunes en backend:

API RESTful
DTOs para desacoplar dominio y transporte
Patrón Fachada
Repository Pattern
Persistencia con JPA / Hibernate
Tecnologías

Java 17
Spring Boot
Spring Web
Spring Data JPA
Hibernate
H2 (entorno local)
Maven

Endpoints principales
Colecciones
GET /colecciones
GET /coleccion/{nombre}
POST /coleccion

Hechos
GET /coleccion/{nombre}/hechos
POST /hecho

Ejecución local
git clone https://github.com/andreaavalos99/FuenteGrupo10.git
cd fuente-service
mvn spring-boot:run

La aplicación se inicia en http://localhost:8080.
La base de datos H2 se crea automáticamente al levantar el proyecto.

Testing
Los endpoints pueden probarse manualmente utilizando Postman u otra herramienta similar.
El servicio está diseñado para ser consumido y testeado de forma aislada.

Alcance del proyecto
Proyecto desarrollado con fines académicos, con foco en:
Diseño de APIs
Modelado de dominio
Arquitectura backend
Integración entre servicios

Notas
Pensado para integrarse con otros microservicios
El código prioriza claridad y estructura

