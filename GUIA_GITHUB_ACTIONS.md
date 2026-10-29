# Guía: configurar GitHub Actions para un pipeline

GitHub Actions automatiza tareas del repositorio mediante workflows. Cada workflow es un archivo YAML ubicado en `.github/workflows/` y puede ejecutarse al hacer `push`, abrir o actualizar un pull request, o manualmente desde GitHub.

## 1. Crear el workflow

En la raíz del repositorio crea la carpeta `.github/workflows/` y dentro el archivo `ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:

permissions:
  contents: read

jobs:
  test:
    name: Compilar y probar microservicios
    runs-on: ubuntu-latest
    steps:
      - name: Descargar el repositorio
        uses: actions/checkout@v4

      - name: Configurar Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Probar API Gateway
        run: mvn -B -f pom.xml -pl api-gateway -am verify

      - name: Probar Config Server
        run: mvn -B -f pom.xml -pl config-server -am verify

      - name: Probar Discovery Server
        run: mvn -B -f pom.xml -pl discovery-server -am verify

      - name: Probar Socio Service
        run: mvn -B -f pom.xml -pl microservices/socio-service -am verify

      - name: Probar Cuentas Service
        run: mvn -B -f pom.xml -pl microservices/cuentas-service -am verify

      - name: Probar Prestamo Service
        run: mvn -B -f pom.xml -pl microservices/prestamo-service -am verify

      - name: Probar Configuration Service
        run: mvn -B -f pom.xml -pl microservices/configuration-service -am verify

      - name: Probar Usuarios Service
        run: mvn -B -f pom.xml -pl microservices/usuarios-service -am verify
```

Los comandos se ejecutan desde la raíz y usan las rutas de los módulos anidados bajo `microservices/`, además de `-am` para incluir dependencias como `common-web`. Maven descarga las dependencias, compila los módulos seleccionados y ejecuta sus pruebas. GitHub muestra el resultado y los logs en la pestaña **Actions**, además del estado del pull request.

> Si la rama principal del repositorio no se llama `main`, cambia ese nombre en los filtros `push` y `pull_request`.

## 2. Subir los cambios y revisar el pipeline

Guarda el archivo como `.github/workflows/ci.yml`, luego agrega y sube el cambio:

```bash
git add .github/workflows/ci.yml
git commit -m "Add CI pipeline with GitHub Actions"
git push
```

En GitHub, abre **Actions** para consultar la ejecución y sus logs. Para ejecutarlo manualmente, selecciona el workflow **CI** y pulsa **Run workflow**.

Para exigir que el pipeline pase antes de integrar pull requests, configura una regla para la rama en **Settings > Branches** y marca como requerido el check **Compilar y probar microservicios**.

## 3. Secretos y variables

No escribas contraseñas ni tokens en el YAML ni en el repositorio. Para usar credenciales, abre **Settings > Secrets and variables > Actions**, crea un *repository secret* y refiérelo en el workflow así:

```yaml
${{ secrets.NOMBRE_DEL_SECRET }}
```

Usa **Variables** para valores no sensibles y **Secrets** para credenciales. Si luego agregas publicación de imágenes Docker en GHCR o despliegue, crea un job separado que dependa de que las pruebas hayan pasado y otorga sólo los permisos necesarios, por ejemplo `packages: write` para publicar en GitHub Container Registry.

## 4. Validar o construir Docker (opcional)

Para verificar que Docker Compose sea válido, agrega este paso al job después de configurar Docker o del checkout:

```yaml
- name: Validar Docker Compose
  run: docker compose config --quiet
```

Para construir las imágenes durante CI:

```yaml
- name: Construir imágenes
  run: docker compose build
```

La construcción completa de imágenes tarda más que las pruebas Maven. Para despliegues reales, configura un entorno de GitHub en **Settings > Environments**, protege sus secretos y permite el despliegue sólo desde ramas autorizadas.

## Mantenimiento

Los ejemplos usan versiones estables comunes de las acciones (`actions/checkout@v4` y `actions/setup-java@v4`). Actualízalas de acuerdo con las políticas de seguridad y mantenimiento del repositorio.
