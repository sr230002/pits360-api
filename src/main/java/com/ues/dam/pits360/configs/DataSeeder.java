package com.ues.dam.pits360.configs;

import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.repositories.CitaRepository;
import com.ues.dam.pits360.repositories.OrdenRepository;
import com.ues.dam.pits360.repositories.ServicioRepository;
import com.ues.dam.pits360.repositories.TallerRepository;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.repositories.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seed demo (spec §7, ADR-11): si hay usuarios, no duplica nada.
 * Ids UUID fijos hardcodeados para smoke test reproducible.
 */
@Component
@Profile("jpa")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private static final String U_CLIENTE = "11111111-1111-1111-1111-111111111111";
    private static final String U_TECNICO = "22222222-2222-2222-2222-222222222222";
    private static final String U_ADMIN   = "33333333-3333-3333-3333-333333333333";
    private static final String TALLER_1  = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1";
    private static final String TALLER_2  = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2";
    private static final String SERV_1    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01";
    private static final String SERV_2    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02";
    private static final String SERV_3    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03";
    private static final String SERV_4    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04";
    private static final String VEH_1     = "cccccccc-cccc-cccc-cccc-ccccccccccc1";
    private static final String VEH_2     = "cccccccc-cccc-cccc-cccc-ccccccccccc2";
    private static final String ORD_1     = "dddddddd-dddd-dddd-dddd-dddddddddd01";
    private static final String ORD_2     = "dddddddd-dddd-dddd-dddd-dddddddddd02";
    private static final String CITA_1    = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1";
    private static final String CITA_2    = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2";

    private final UsuarioRepository usuarioRepository;
    private final TallerRepository tallerRepository;
    private final ServicioRepository servicioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final OrdenRepository ordenRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            log.info("Seed ya aplicado");
            return;
        }

        var hash = passwordEncoder.encode("pits360");
        usuarioRepository.saveAll(List.of(
                Usuario.builder().id(U_CLIENTE).nombre("Ana Cliente Demo").email("cliente@pits360.com")
                        .passwordHash(hash).rol("cliente").build(),
                Usuario.builder().id(U_TECNICO).nombre("Beto Tecnico Demo").email("tecnico@pits360.com")
                        .passwordHash(hash).rol("tecnico").build(),
                Usuario.builder().id(U_ADMIN).nombre("Carla Admin Demo").email("admin@pits360.com")
                        .passwordHash(hash).rol("admin").build()));

        tallerRepository.saveAll(List.of(
                Taller.builder().id(TALLER_1).nombre("AutoCentro Pits Santa Ana")
                        .direccion("Av. Las Palmas 15, Santa Ana").telefono("7889-1234").rating(4.6).build(),
                Taller.builder().id(TALLER_2).nombre("Mecanica Rumen Palmar")
                        .direccion("Calle Los Naranjos 42, Metapan").telefono("7122-5678").rating(4.8).build()));

        servicioRepository.saveAll(List.of(
                Servicio.builder().id(SERV_1).nombre("Cambio de aceite y filtros")
                        .descripcion("Aceite 5W30 sintetico + filtro de aceite y aire").precio(45.0).duracion(60).build(),
                Servicio.builder().id(SERV_2).nombre("Frenos completos")
                        .descripcion("Pastillas y discos delanteros, purgado de sistema").precio(120.0).duracion(180).build(),
                Servicio.builder().id(SERV_3).nombre("Diagnostico computarizado")
                        .descripcion("Escaneo OBD-II y lectura de codigos de error").precio(30.0).duracion(45).build(),
                Servicio.builder().id(SERV_4).nombre("Alineacion y balanceo")
                        .descripcion("Geo 3D y balanceo de las 4 ruedas en pista").precio(55.0).duracion(90).build()));

        vehiculoRepository.saveAll(List.of(
                Vehiculo.builder().id(VEH_1).placa("P123-456").marca("Toyota").modelo("Corolla")
                        .anio(2020).kilometraje(48000).build(),
                Vehiculo.builder().id(VEH_2).placa("C987-321").marca("Honda").modelo("HR-V")
                        .anio(2022).kilometraje(21000).build()));

        ordenRepository.saveAll(List.of(
                Orden.builder().id(ORD_1).numero("ORD-2026-0001").estado("EN_PROCESO")
                        .vehiculoId(VEH_1).tallerId(TALLER_1).fecha("2026-10-15").total(125.5).build(),
                Orden.builder().id(ORD_2).numero("ORD-2026-0002").estado("FINALIZADO")
                        .vehiculoId(VEH_2).tallerId(TALLER_2).fecha("2026-10-10").total(89.9).build()));

        citaRepository.saveAll(List.of(
                Cita.builder().id(CITA_1).fecha("2026-10-20").hora("09:00").estado("PROGRAMADA")
                        .vehiculoId(VEH_1).tallerId(TALLER_1).build(),
                Cita.builder().id(CITA_2).fecha("2026-10-22").hora("14:30").estado("CONFIRMADA")
                        .vehiculoId(VEH_2).tallerId(TALLER_2).build()));

        log.info("Seed aplicado: 3 usuarios, 2 talleres, 4 servicios, 2 vehiculos, 2 ordenes, 2 citas");
    }
}
