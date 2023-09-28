# Clase AuthorizationSecurityConfig

Esta clase Java configura un servidor de autorización OAuth2 utilizando Spring Security y Spring OAuth2. A continuación, se describe el propósito de cada sección de código:

## Importaciones
Se importan las clases y paquetes necesarios para la configuración, incluyendo clases de seguridad de Spring, clases relacionadas con OAuth2, y clases para manejar claves y tokens JWT.

## Anotaciones
- `@Configuration`: Indica que esta clase es una configuración de Spring.
- `@Slf4j`: Habilita el registro de logs utilizando Lombok.

## Configuración del Filtro de Seguridad para el Servidor de Autorización OAuth2
- Se define un filtro de seguridad (`authorizationServerSecurityFilterChain`) para el servidor de autorización OAuth2. Este filtro establece las reglas de seguridad y cómo manejar las excepciones.
- Se utiliza `OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)` para aplicar la configuración de seguridad predeterminada para el servidor de autorización OAuth2.
- Se configura el uso de OpenID Connect 1.0 (`oidc(Customizer.withDefaults())`).
- Se configura el manejo de excepciones para redirigir a la página de inicio de sesión cuando no se está autenticado desde el punto de autorización.
- Se configura el servidor de recursos OAuth2 para aceptar tokens de acceso JWT.

## Configuración del Filtro de Seguridad Predeterminado para la Aplicación
- Se define un filtro de seguridad (`defaultSecurityFilterChain`) para la aplicación, que controla cómo se autentican los usuarios y cómo se manejan las solicitudes de acceso.
- Se configura la autorización para requerir autenticación para todas las solicitudes (`authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())`).
- Se configura la autenticación basada en formularios (`formLogin(Customizer.withDefaults())`).

## Configuración de Detalles de Usuario para Autenticación
- Se define un servicio de detalles de usuario (`userDetailsService`) que proporciona detalles de usuario en memoria. Esto es solo para fines de demostración y debe reemplazarse en un entorno de producción.

## Configuración de Clientes Registrados
- Se configura un repositorio de clientes registrados (`registeredClientRepository`) con detalles sobre los clientes OAuth2 registrados, incluyendo ID de cliente, secreto, alcance, etc.

## Configuración de Claves Públicas y Privadas
- Se generan claves RSA (una pública y una privada) que se utilizarán para firmar y verificar tokens JWT.
- Se crea un decodificador JWT que utiliza estas claves para validar los tokens de acceso.

## Configuración de Propiedades del Servidor de Autorización OAuth2
- Se configuran las propiedades del servidor de autorización OAuth2 utilizando `AuthorizationServerSettings`.

En resumen, esta clase proporciona una configuración básica para un servidor de autorización OAuth2 utilizando Spring Security y Spring OAuth2. Se deben realizar ajustes y personalizaciones adicionales para adaptarla a las necesidades específicas de una aplicación.
