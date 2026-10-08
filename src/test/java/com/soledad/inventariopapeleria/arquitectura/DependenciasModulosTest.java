package com.soledad.inventariopapeleria.arquitectura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class DependenciasModulosTest {

    private final JavaClasses clases = new ClassFileImporter().importPackages("com.soledad.inventariopapeleria");

    @Test
    void productoNuncaDebeDependerDeInventarioOVenta() {
        ArchRule regla = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..producto..")
                .should().dependOnClassesThat().resideInAnyPackage("..inventario..", "..venta..")
                .allowEmptyShould(true);
        regla.check(clases);
    }

    @Test
    void ventaNoDebeModificarStockDirectamente() {
        ArchRule regla = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..venta..")
                .should().accessClassesThat().haveSimpleNameEndingWith("StockRepository")
                .allowEmptyShould(true);
        regla.check(clases);
    }

    @Test
    void reporteNoDebeUsarServiciosDeOtrosModulos() {
        ArchRule regla = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..reporte..")
                .and().haveSimpleNameNotEndingWith("ExportadorExcel")
                .and().haveSimpleNameNotEndingWith("ExportadorPdf")
                .and().resideOutsideOfPackage("..exportacion..")
                .should().dependOnClassesThat().resideInAnyPackage("..venta..servicio..", "..inventario..servicio..",
                        "..producto..servicio..", "..usuario..servicio..", "..configuracion..servicio..",
                        "..proveedor..servicio..", "..cliente..servicio..")
                .allowEmptyShould(true);
        regla.check(clases);
    }
}
