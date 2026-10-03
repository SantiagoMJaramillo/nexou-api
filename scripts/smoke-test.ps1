<#
.SYNOPSIS
    Pruebas de humo de NEXOU API: documentación, seed, CRUD, métodos personalizados,
    reglas de negocio, manejo de errores y CORS.

.DESCRIPTION
    Requiere la API corriendo con los datos de la seed (DataSeederConfig).
    El script crea datos temporales y los elimina al final, así que se puede
    ejecutar varias veces sobre la misma base de datos.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts\smoke-test.ps1
    powershell -ExecutionPolicy Bypass -File scripts\smoke-test.ps1 -BaseUrl http://localhost:8081
#>
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

$script:Client = New-Object System.Net.Http.HttpClient
$script:Pasaron = 0
$script:Fallaron = New-Object System.Collections.Generic.List[string]
$Stamp = Get-Date -Format "yyyyMMddHHmmss"

# ---------------------------------------------------------------------------
# Utilidades
# ---------------------------------------------------------------------------

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [hashtable]$Headers = @{}
    )
    $request = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::new($Method), "$BaseUrl$Path")
    foreach ($key in $Headers.Keys) {
        [void]$request.Headers.TryAddWithoutValidation($key, $Headers[$key])
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 6 -Compress
        $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, "application/json")
    }
    $response = $script:Client.SendAsync($request).GetAwaiter().GetResult()
    $text = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $data = $null
    if ($text -and ($text.TrimStart().StartsWith("{") -or $text.TrimStart().StartsWith("["))) {
        $data = $text | ConvertFrom-Json
    }
    return [pscustomobject]@{
        Status   = [int]$response.StatusCode
        Text     = $text
        Json     = $data
        Response = $response
    }
}

function Test-Condicion {
    param([string]$Nombre, [bool]$Condicion, [string]$Detalle = "")
    if ($Condicion) {
        $script:Pasaron++
        Write-Host "  [OK]    $Nombre" -ForegroundColor Green
    } else {
        $script:Fallaron.Add("$Nombre  $Detalle")
        Write-Host "  [FALLA] $Nombre  $Detalle" -ForegroundColor Red
    }
}

function Test-Status {
    param([string]$Nombre, $Resultado, [int]$Esperado)
    $detalle = "(esperado $Esperado, recibido $($Resultado.Status)) $($Resultado.Text)"
    if ($detalle.Length -gt 300) { $detalle = $detalle.Substring(0, 300) + "..." }
    Test-Condicion $Nombre ($Resultado.Status -eq $Esperado) $detalle
}

function Test-Error {
    # Verifica el código HTTP y que el cuerpo tenga el formato {"mensaje": "..."}
    param([string]$Nombre, $Resultado, [int]$Esperado)
    Test-Status $Nombre $Resultado $Esperado
    Test-Condicion "  ...responde con {mensaje}" ($null -ne $Resultado.Json -and $Resultado.Json.mensaje) $Resultado.Text
}

function Get-Count($Resultado) {
    if ($null -eq $Resultado.Json) { return -1 }
    return @($Resultado.Json).Count
}

function Seccion($Titulo) {
    Write-Host ""
    Write-Host "== $Titulo ==" -ForegroundColor Cyan
}

# ---------------------------------------------------------------------------
Seccion "0. Disponibilidad"
# ---------------------------------------------------------------------------
try {
    $docs = Invoke-Api GET "/v3/api-docs"
} catch {
    Write-Host "La API no responde en $BaseUrl. Arranquela primero (./mvnw spring-boot:run)." -ForegroundColor Red
    exit 1
}
Test-Status "GET /v3/api-docs" $docs 200

# ---------------------------------------------------------------------------
Seccion "1. Documentacion OpenAPI / Scalar"
# ---------------------------------------------------------------------------
Test-Condicion "La especificacion tiene el titulo de NEXOU" ($docs.Text -match "NEXOU API")
foreach ($ruta in @("/api/usuarios", "/api/configuraciones", "/api/categorias", "/api/libros", "/api/equipos", "/api/reservas-libros", "/api/reservas-equipos")) {
    Test-Condicion "OpenAPI documenta $ruta" ($null -ne $docs.Json.paths.$ruta)
}
Test-Condicion "OpenAPI tiene 7 grupos (@Tag)" (@($docs.Json.tags).Count -ge 7) "tags: $(@($docs.Json.tags).Count)"
Test-Status "GET /scalar" (Invoke-Api GET "/scalar") 200
Test-Status "GET /swagger-ui.html" (Invoke-Api GET "/swagger-ui.html") 200

# ---------------------------------------------------------------------------
Seccion "2. Datos de la seed"
# ---------------------------------------------------------------------------
$esperados = [ordered]@{
    "/api/usuarios" = 2; "/api/configuraciones" = 2; "/api/categorias" = 3; "/api/libros" = 5;
    "/api/equipos" = 3; "/api/reservas-libros" = 3; "/api/reservas-equipos" = 2
}
foreach ($ruta in $esperados.Keys) {
    $r = Invoke-Api GET $ruta
    Test-Condicion "GET $ruta devuelve $($esperados[$ruta]) registros" ($r.Status -eq 200 -and (Get-Count $r) -eq $esperados[$ruta]) "(status $($r.Status), registros $(Get-Count $r))"
}
$usuarios = Invoke-Api GET "/api/usuarios"
Test-Condicion "La contrasena no se expone en las respuestas" (-not ($usuarios.Text -match "contrasena"))

# ---------------------------------------------------------------------------
Seccion "3. Metodos personalizados"
# ---------------------------------------------------------------------------
$r = Invoke-Api GET "/api/usuarios/correo/laura.restrepo@cesde.net"; Test-Status "GET /api/usuarios/correo/{correo}" $r 200
$uLaura = $r.Json.id
$r = Invoke-Api GET "/api/usuarios/correo/carlos.gomez@cesde.net"; $uCarlos = $r.Json.id
$r = Invoke-Api GET "/api/libros/isbn/978-8478290598"; Test-Status "GET /api/libros/isbn/{isbn}" $r 200
$lPatrones = $r.Json.id
$dispPatrones = $r.Json.cantidadDisponible
$r = Invoke-Api GET "/api/libros/isbn/978-0000000001"; $lSql = $r.Json.id
$r = Invoke-Api GET "/api/categorias/nombre/Bases%20de%20Datos"; Test-Status "GET /api/categorias/nombre/{nombre}" $r 200
$r = Invoke-Api GET "/api/equipos/disponibles"; Test-Status "GET /api/equipos/disponibles" $r 200
$tablet = @($r.Json) | Where-Object { $_.nomEquipo -eq "Tablet Galaxy Tab" } | Select-Object -First 1
$eTablet = $tablet.id
Test-Condicion "Los equipos disponibles tienen stock > 0" (@(@($r.Json) | Where-Object { $_.cantidadDisponible -le 0 }).Count -eq 0)
$r = Invoke-Api GET "/api/configuraciones/usuario/$uLaura"; Test-Status "GET /api/configuraciones/usuario/{usuarioId}" $r 200
$r = Invoke-Api GET "/api/reservas-libros/usuario/$uLaura"; Test-Status "GET /api/reservas-libros/usuario/{usuarioId}" $r 200
$r = Invoke-Api GET "/api/reservas-equipos/usuario/$uLaura"; Test-Status "GET /api/reservas-equipos/usuario/{usuarioId}" $r 200
$r = Invoke-Api GET "/api/usuarios/$uLaura/validar-limite"; Test-Status "GET /api/usuarios/{id}/validar-limite (2 activas)" $r 200

if (-not ($uLaura -and $uCarlos -and $lPatrones -and $lSql -and $eTablet)) {
    Write-Host "No se encontraron los datos de la seed; se detienen las pruebas. Use una base de datos vacia." -ForegroundColor Red
    exit 1
}

# ---------------------------------------------------------------------------
Seccion "4. Manejo de errores (404 / 400 con {mensaje})"
# ---------------------------------------------------------------------------
Test-Error "GET usuario inexistente -> 404" (Invoke-Api GET "/api/usuarios/999999") 404
Test-Error "GET libro por ISBN inexistente -> 404" (Invoke-Api GET "/api/libros/isbn/NO-EXISTE") 404
Test-Error "POST usuario con correo duplicado -> 400" (Invoke-Api POST "/api/usuarios" @{ nombreUsuario = "Copia"; correo = "laura.restrepo@cesde.net"; contrasena = "x" }) 400
Test-Error "POST reserva con body vacio (@Valid) -> 400" (Invoke-Api POST "/api/reservas-libros" @{}) 400
Test-Error "POST reserva de libro inexistente -> 404" (Invoke-Api POST "/api/reservas-libros" @{ usuarioId = $uCarlos; libroId = 999999; diasPrestamo = 3 }) 404
Test-Error "POST reserva que supera diasPrestamoMax -> 400" (Invoke-Api POST "/api/reservas-libros" @{ usuarioId = $uCarlos; libroId = $lSql; diasPrestamo = 30 }) 400

# ---------------------------------------------------------------------------
Seccion "5. Flujo de reserva de libro: prestar, renovar, devolver"
# ---------------------------------------------------------------------------
$r = Invoke-Api POST "/api/reservas-libros" @{ usuarioId = $uCarlos; libroId = $lPatrones; diasPrestamo = 5 }
Test-Status "POST /api/reservas-libros -> 201" $r 201
$rLibro = $r.Json.id
$fechaInicial = $r.Json.fechaEntregaEsperada
Test-Condicion "La reserva nace ACTIVA" ($r.Json.estadoReserva -eq "ACTIVA")
$r = Invoke-Api GET "/api/libros/$lPatrones"
Test-Condicion "Prestar descuenta 1 del stock del libro" ($r.Json.cantidadDisponible -eq ($dispPatrones - 1)) "(antes $dispPatrones, ahora $($r.Json.cantidadDisponible))"

$r = Invoke-Api PATCH "/api/reservas-libros/$rLibro/renovacion" @{ diasExtra = 5 }
Test-Status "PATCH /renovacion con 5 dias -> 200" $r 200
Test-Condicion "La renovacion mueve la fecha de entrega" ($r.Json.fechaEntregaEsperada -ne $fechaInicial)
Test-Error "PATCH /renovacion con 20 dias -> 400" (Invoke-Api PATCH "/api/reservas-libros/$rLibro/renovacion" @{ diasExtra = 20 }) 400

$r = Invoke-Api PUT "/api/reservas-libros/$rLibro" @{ tipoPrestamo = "Sala"; proposito = "Prueba smoke" }
Test-Status "PUT /api/reservas-libros/{id} -> 200" $r 200
Test-Condicion "PUT actualiza el proposito" ($r.Json.proposito -eq "Prueba smoke")
Test-Error "DELETE de una reserva ACTIVA -> 400" (Invoke-Api DELETE "/api/reservas-libros/$rLibro") 400

$r = Invoke-Api PATCH "/api/reservas-libros/$rLibro/devolucion"
Test-Status "PATCH /devolucion -> 200" $r 200
Test-Condicion "La reserva queda DEVUELTO con fecha real" ($r.Json.estadoReserva -eq "DEVUELTO" -and $r.Json.fechaDevolucionReal)
Test-Error "Devolver dos veces -> 400" (Invoke-Api PATCH "/api/reservas-libros/$rLibro/devolucion") 400
$r = Invoke-Api GET "/api/libros/$lPatrones"
Test-Condicion "Devolver repone el stock del libro" ($r.Json.cantidadDisponible -eq $dispPatrones)
Test-Status "DELETE de una reserva devuelta -> 204" (Invoke-Api DELETE "/api/reservas-libros/$rLibro") 204

# ---------------------------------------------------------------------------
Seccion "6. Flujo de reserva de equipo"
# ---------------------------------------------------------------------------
$dispTablet = (Invoke-Api GET "/api/equipos/$eTablet").Json.cantidadDisponible
Test-Error "Reserva de 2h59m con maximo de 2h -> 400" (Invoke-Api POST "/api/reservas-equipos" @{ usuarioId = $uCarlos; equipoId = $eTablet; horaInicio = "2030-01-15T08:00:00"; horaFin = "2030-01-15T10:59:00" }) 400
Test-Error "Hora fin anterior a hora inicio -> 400" (Invoke-Api POST "/api/reservas-equipos" @{ usuarioId = $uCarlos; equipoId = $eTablet; horaInicio = "2030-01-15T10:00:00"; horaFin = "2030-01-15T09:00:00" }) 400
$r = Invoke-Api POST "/api/reservas-equipos" @{ usuarioId = $uCarlos; equipoId = $eTablet; horaInicio = "2030-01-15T08:00:00"; horaFin = "2030-01-15T09:00:00" }
Test-Status "POST /api/reservas-equipos (1h) -> 201" $r 201
$rEquipo = $r.Json.id
Test-Condicion "Prestar descuenta 1 del stock del equipo" ((Invoke-Api GET "/api/equipos/$eTablet").Json.cantidadDisponible -eq ($dispTablet - 1))
$r = Invoke-Api PUT "/api/reservas-equipos/$rEquipo" @{ lugarEntrega = "Aula 101"; proposito = "Prueba smoke" }
Test-Status "PUT /api/reservas-equipos/{id} -> 200" $r 200
Test-Status "PATCH /api/reservas-equipos/{id}/devolucion -> 200" (Invoke-Api PATCH "/api/reservas-equipos/$rEquipo/devolucion") 200
Test-Condicion "Devolver repone el stock del equipo" ((Invoke-Api GET "/api/equipos/$eTablet").Json.cantidadDisponible -eq $dispTablet)
Test-Status "DELETE de la reserva devuelta -> 204" (Invoke-Api DELETE "/api/reservas-equipos/$rEquipo") 204

# ---------------------------------------------------------------------------
Seccion "7. Limite global de 3 reservas activas"
# ---------------------------------------------------------------------------
$r = Invoke-Api POST "/api/reservas-equipos" @{ usuarioId = $uLaura; equipoId = $eTablet; horaInicio = "2030-01-16T08:00:00"; horaFin = "2030-01-16T09:00:00" }
Test-Status "Laura llega a 3 reservas activas -> 201" $r 201
$rLimite = $r.Json.id
Test-Error "validar-limite con 3 activas -> 400" (Invoke-Api GET "/api/usuarios/$uLaura/validar-limite") 400
Test-Error "Una 4a reserva es rechazada -> 400" (Invoke-Api POST "/api/reservas-libros" @{ usuarioId = $uLaura; libroId = $lPatrones; diasPrestamo = 3 }) 400
if ($rLimite) {
    [void](Invoke-Api PATCH "/api/reservas-equipos/$rLimite/devolucion")
    [void](Invoke-Api DELETE "/api/reservas-equipos/$rLimite")
}
Test-Status "Tras devolver, validar-limite -> 200" (Invoke-Api GET "/api/usuarios/$uLaura/validar-limite") 200

# ---------------------------------------------------------------------------
Seccion "8. CRUD de catalogo: categorias, libros y equipos"
# ---------------------------------------------------------------------------
$nombreCat = "Prueba Smoke $Stamp"
$r = Invoke-Api POST "/api/categorias" @{ nombre = $nombreCat; descripcion = "Temporal" }
Test-Status "POST /api/categorias -> 201" $r 201
$cat = $r.Json.id
Test-Error "POST categoria con nombre repetido -> 400" (Invoke-Api POST "/api/categorias" @{ nombre = $nombreCat }) 400
Test-Status "PUT /api/categorias/{id} -> 200" (Invoke-Api PUT "/api/categorias/$cat" @{ nombre = $nombreCat; descripcion = "Editada" }) 200

$isbn = "SMOKE-$Stamp"
$libroBody = @{ nomLibro = "Libro de prueba"; autor = "Smoke"; isbn = $isbn; cantidadTotal = 2; diasPrestamoMax = 5;
                ubicacionFisica = @{ sede = "Sede Bello"; piso = "1"; referencia = "Temporal" }; categorias = @(@{ id = $cat }) }
$r = Invoke-Api POST "/api/libros" $libroBody
Test-Status "POST /api/libros con categoria -> 201" $r 201
$libro = $r.Json.id
Test-Condicion "Sin cantidadDisponible, inicia igual a la total" ($r.Json.cantidadDisponible -eq 2)
Test-Condicion "El libro queda asociado a la categoria" (@($r.Json.categorias).Count -eq 1)
Test-Error "POST libro con ISBN repetido -> 400" (Invoke-Api POST "/api/libros" $libroBody) 400
$libroBody.cantidadDisponible = 5
Test-Error "PUT libro con disponible > total -> 400" (Invoke-Api PUT "/api/libros/$libro" $libroBody) 400
Test-Error "DELETE categoria con libros -> 400" (Invoke-Api DELETE "/api/categorias/$cat") 400
Test-Status "DELETE /api/libros/{id} -> 204" (Invoke-Api DELETE "/api/libros/$libro") 204
Test-Status "DELETE /api/categorias/{id} -> 204" (Invoke-Api DELETE "/api/categorias/$cat") 204
Test-Error "GET categoria eliminada -> 404" (Invoke-Api GET "/api/categorias/$cat") 404

Test-Error "POST equipo con cantidadTotal 0 -> 400" (Invoke-Api POST "/api/equipos" @{ nomEquipo = "X"; cantidadTotal = 0; duracionMaximaHrs = 2 }) 400
$r = Invoke-Api POST "/api/equipos" @{ nomEquipo = "Equipo smoke $Stamp"; marca = "Test"; cantidadTotal = 1; duracionMaximaHrs = 1.5; estadoEquipo = "Operativo" }
Test-Status "POST /api/equipos -> 201" $r 201
$equipo = $r.Json.id
Test-Status "PUT /api/equipos/{id} -> 200" (Invoke-Api PUT "/api/equipos/$equipo" @{ nomEquipo = "Equipo smoke $Stamp"; cantidadTotal = 2; duracionMaximaHrs = 2 }) 200
Test-Status "DELETE /api/equipos/{id} -> 204" (Invoke-Api DELETE "/api/equipos/$equipo") 204

# ---------------------------------------------------------------------------
Seccion "9. CRUD de usuarios y configuracion"
# ---------------------------------------------------------------------------
$correo = "smoke.$Stamp@cesde.net"
$r = Invoke-Api POST "/api/usuarios" @{ nombreUsuario = "Usuario Smoke"; correo = $correo; contrasena = "Secreta123";
                                         configuracionUsuario = @{ idioma = "en"; tema = "oscuro"; notificacionesActivas = $true } }
Test-Status "POST /api/usuarios con configuracion -> 201" $r 201
$usuario = $r.Json.id
Test-Condicion "La respuesta no incluye la contrasena" (-not ($r.Text -match "Secreta123|contrasena"))
$r = Invoke-Api GET "/api/configuraciones/usuario/$usuario"
Test-Condicion "La configuracion se guardo por cascada" ($r.Status -eq 200 -and $r.Json.idioma -eq "en")
$config = $r.Json.id
Test-Error "POST segunda configuracion al mismo usuario -> 400" (Invoke-Api POST "/api/configuraciones" @{ idioma = "es"; usuario = @{ id = $usuario } }) 400
Test-Status "PUT /api/configuraciones/{id} -> 200" (Invoke-Api PUT "/api/configuraciones/$config" @{ idioma = "es"; tema = "claro"; notificacionesActivas = $false }) 200
Test-Status "DELETE /api/configuraciones/{id} -> 204" (Invoke-Api DELETE "/api/configuraciones/$config") 204
$r = Invoke-Api POST "/api/configuraciones" @{ idioma = "es"; tema = "claro"; usuario = @{ id = $usuario } }
Test-Status "POST /api/configuraciones -> 201" $r 201
$r = Invoke-Api PUT "/api/usuarios/$usuario" @{ nombreUsuario = "Usuario Smoke"; correo = $correo; telefono = "3000000000" }
Test-Status "PUT /api/usuarios/{id} -> 200" $r 200
Test-Error "DELETE usuario con reservas -> 400" (Invoke-Api DELETE "/api/usuarios/$uLaura") 400
Test-Status "DELETE /api/usuarios/{id} sin reservas -> 204" (Invoke-Api DELETE "/api/usuarios/$usuario") 204
Test-Error "La configuracion se borro con el usuario -> 404" (Invoke-Api GET "/api/configuraciones/usuario/$usuario") 404

# ---------------------------------------------------------------------------
Seccion "10. CORS (frontend en otro puerto)"
# ---------------------------------------------------------------------------
$r = Invoke-Api OPTIONS "/api/reservas-libros/1/devolucion" $null @{ "Origin" = "http://localhost:5173"; "Access-Control-Request-Method" = "PATCH" }
$permitidos = ""
if ($r.Response.Headers.Contains("Access-Control-Allow-Methods")) {
    $permitidos = ($r.Response.Headers.GetValues("Access-Control-Allow-Methods") -join ",")
}
Test-Condicion "Preflight CORS permite PATCH" ($r.Status -eq 200 -and $permitidos -match "PATCH") "(status $($r.Status), metodos '$permitidos')"

# ---------------------------------------------------------------------------
Write-Host ""
$total = $script:Pasaron + $script:Fallaron.Count
if ($script:Fallaron.Count -eq 0) {
    Write-Host "RESULTADO: $($script:Pasaron)/$total pruebas OK" -ForegroundColor Green
    exit 0
} else {
    Write-Host "RESULTADO: $($script:Fallaron.Count) de $total pruebas fallaron:" -ForegroundColor Red
    $script:Fallaron | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    if ($env:GITHUB_ACTIONS -eq "true") {
        # Anotación visible en la pestaña Checks del PR
        $texto = ($script:Fallaron -join "`n").Replace("%", "%25").Replace("`r", "%0D").Replace("`n", "%0A")
        Write-Host "::error title=Pruebas de humo ($($script:Fallaron.Count) fallas)::$texto"
    }
    exit 1
}
