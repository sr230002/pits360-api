package com.ues.dam.integration.firebase.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.google.cloud.firestore.Firestore;

import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.entities.Vehiculo;

import lombok.extern.slf4j.Slf4j;

/**
 * Seed demo sobre Firestore (spec §7, ADR-11): si la colección de usuarios ya
 * tiene datos, no siembra nada. Ids UUID fijos hardcodeados, así un segundo
 * arranque con la colección vacía a medias no duplica documentos.
 *
 * <p>
 * La contraseña demo nunca se registra en logs: solo su hash BCrypt.
 *
 * <p>
 * {@link FirestoreRepository} no es bean: el seeder construye el suyo por
 * colección, igual que los servicios Firestore.
 */
@Slf4j
@Component
@Profile("!jpa")
public class FirestoreDataSeeder implements ApplicationRunner {

	/** Contraseña demo de los tres usuarios sembrados. */
	private static final String PASSWORD_DEMO = "pits3601";

	private static final String U_CLIENTE = "11111111-1111-1111-1111-111111111111";
	private static final String U_TECNICO = "22222222-2222-2222-2222-222222222222";
	private static final String U_ADMIN = "33333333-3333-3333-3333-333333333333";
	private static final String TALLER_1 = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1";
	private static final String TALLER_2 = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2";
	private static final String SERV_1 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01";
	private static final String SERV_2 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02";
	private static final String SERV_3 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03";
	private static final String SERV_4 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04";
	private static final String VEH_1 = "cccccccc-cccc-cccc-cccc-ccccccccccc1";
	private static final String VEH_2 = "cccccccc-cccc-cccc-cccc-ccccccccccc2";
	private static final String ORD_1 = "dddddddd-dddd-dddd-dddd-dddddddddd01";
	private static final String ORD_2 = "dddddddd-dddd-dddd-dddd-dddddddddd02";
	private static final String CITA_1 = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1";
	private static final String CITA_2 = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2";

	private final FirestoreRepository<Usuario> usuarios;
	private final FirestoreRepository<Taller> talleres;
	private final FirestoreRepository<Servicio> servicios;
	private final FirestoreRepository<Vehiculo> vehiculos;
	private final FirestoreRepository<Cita> citas;
	private final FirestoreRepository<Orden> ordenes;
	private final PasswordEncoder passwordEncoder;

	/** Constructor de Spring: arma un repositorio por colección. */
	@Autowired
	public FirestoreDataSeeder(Firestore firestore, PasswordEncoder passwordEncoder) {
		this(new FirestoreRepository<>(firestore, FirestoreCollections.USUARIOS, Usuario.class, Usuario::getId,
				Usuario::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.TALLERES, Taller.class, Taller::getId,
						Taller::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.SERVICIOS, Servicio.class, Servicio::getId,
						Servicio::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.VEHICULOS, Vehiculo.class, Vehiculo::getId,
						Vehiculo::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.CITAS, Cita.class, Cita::getId, Cita::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.ORDENES, Orden.class, Orden::getId,
						Orden::setId),
				passwordEncoder);
	}

	/** Constructor de test: repositorios ya construidos. */
	public FirestoreDataSeeder(FirestoreRepository<Usuario> usuarios, FirestoreRepository<Taller> talleres,
			FirestoreRepository<Servicio> servicios, FirestoreRepository<Vehiculo> vehiculos,
			FirestoreRepository<Cita> citas, FirestoreRepository<Orden> ordenes, PasswordEncoder passwordEncoder) {
		this.usuarios = usuarios;
		this.talleres = talleres;
		this.servicios = servicios;
		this.vehiculos = vehiculos;
		this.citas = citas;
		this.ordenes = ordenes;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!usuarios.findAll().isEmpty()) {
			log.info("Seed omitido: la colección de usuarios ya tiene datos");
			return;
		}

		sembrarUsuarios();
		sembrarTalleres();
		sembrarServicios();
		sembrarVehiculos();
		sembrarCitas();
		sembrarOrdenes();

		log.info("Seed aplicado en Firestore: 3 usuarios, 2 talleres, 4 servicios, 2 vehiculos, 2 citas, 2 ordenes");
	}

	private void sembrarUsuarios() {
		String hash = passwordEncoder.encode(PASSWORD_DEMO);
		usuarios.save(Usuario.builder().id(U_CLIENTE).nombre("Ana Cliente Demo").email("cliente@pits360.com")
				.passwordHash(hash).rol("cliente").telefono("7011-2233").ciudad("San Salvador").fotoUrl(null)
				.build());
		usuarios.save(Usuario.builder().id(U_TECNICO).nombre("Beto Tecnico Demo").email("tecnico@pits360.com")
				.passwordHash(hash).rol("tecnico").telefono("7011-3344").ciudad("Santa Ana").fotoUrl(null)
				.build());
		usuarios.save(Usuario.builder().id(U_ADMIN).nombre("Carla Admin Demo").email("admin@pits360.com")
				.passwordHash(hash).rol("admin").telefono("7011-4455").ciudad("San Miguel").fotoUrl(null).build());
	}

	private void sembrarTalleres() {
		talleres.save(Taller.builder().id(TALLER_1).nombre("AutoCentro Pits San Salvador")
				.direccion("Av. Constitucion 4ta Calle, San Salvador").telefono("2222-3344").rating(4.6)
				.latitud(13.6929).longitud(-89.2182).build());
		talleres.save(Taller.builder().id(TALLER_2).nombre("Mecanica Cuscatlan")
				.direccion("Calz. Roosevelt y 4ta Av., San Salvador").telefono("2222-5566").rating(4.8)
				.latitud(13.7013).longitud(-89.2245).build());
	}

	private void sembrarServicios() {
		servicios.save(Servicio.builder().id(SERV_1).nombre("Cambio de aceite y filtros")
				.descripcion("Aceite 5W30 sintetico + filtro de aceite y aire").precio(45.0).duracion(60).build());
		servicios.save(Servicio.builder().id(SERV_2).nombre("Frenos completos")
				.descripcion("Pastillas y discos delanteros, purgado de sistema").precio(120.0).duracion(180).build());
		servicios.save(Servicio.builder().id(SERV_3).nombre("Diagnostico computarizado")
				.descripcion("Escaneo OBD-II y lectura de codigos de error").precio(30.0).duracion(45).build());
		servicios.save(Servicio.builder().id(SERV_4).nombre("Alineacion y balanceo")
				.descripcion("Geo 3D y balanceo de las 4 ruedas en pista").precio(55.0).duracion(90).build());
	}

	private void sembrarVehiculos() {
		vehiculos.save(Vehiculo.builder().id(VEH_1).placa("P123-456").marca("Toyota").modelo("Corolla").anio(2020)
				.kilometraje(48000).build());
		vehiculos.save(Vehiculo.builder().id(VEH_2).placa("C987-321").marca("Honda").modelo("HR-V").anio(2022)
				.kilometraje(21000).build());
	}

	private void sembrarCitas() {
		citas.save(Cita.builder().id(CITA_1).fecha("2026-10-20").hora("09:00").estado("PROGRAMADA")
				.vehiculoId(VEH_1).tallerId(TALLER_1).build());
		citas.save(Cita.builder().id(CITA_2).fecha("2026-10-22").hora("14:30").estado("CONFIRMADA")
				.vehiculoId(VEH_2).tallerId(TALLER_2).build());
	}

	private void sembrarOrdenes() {
		ordenes.save(Orden.builder().id(ORD_1).numero("ORD-2026-0001").estado("EN_PROCESO").vehiculoId(VEH_1)
				.tallerId(TALLER_1).fecha("2026-10-15").total(125.5).tecnicoId(U_TECNICO).citaId(CITA_1)
				.nota("Cliente pide dejar el auto hasta el viernes").trabajos("Cambio de aceite y filtros")
				.metodoPago("EFECTIVO").avance(50).build());
		ordenes.save(Orden.builder().id(ORD_2).numero("ORD-2026-0002").estado("FINALIZADO").vehiculoId(VEH_2)
				.tallerId(TALLER_2).fecha("2026-10-10").total(89.9).tecnicoId(U_TECNICO).citaId(CITA_2)
				.nota("Revision general sin hallazgos").trabajos("Diagnostico computarizado")
				.metodoPago("TARJETA").avance(100).build());
	}

}
