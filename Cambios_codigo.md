#Cambios en esta version de PXLS

Para esta versión de PXLS se trato de modificar el tiempo de espera, luego de que se agotaran los pixeles. 
La idea era poder dejar esta tiempo estatico en 1 segundo. Pero no se pudo implementar este cambio. 

De igual manera se cambiaron algunos parametros con respecto al tiempo, pero no logrando el resultado esperadp.

**Archivo PxlsTimer.java**
se cambio la linea __if (lastRun + ((long) wait * 1000) < System.currentTimeMillis())__
por __if (lastRun + ((long) wait ) < System.currentTimeMillis())__ . Se esperaba que con esto el tiempo de espera disminuyera. 

**Archivo : RateLimitingHandler.java**
se cambio la linea __int timeSeconds = (int) ((bucket.startTime + time * 1000) - System.currentTimeMillis()) / 1000;__
por __int timeSeconds = (int) ((bucket.startTime + time) - System.currentTimeMillis()) / 1000;__
lo cual se esperaba que la variable segundos disminuyera.
