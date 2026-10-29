package com.manudev.cooperativa.prestamo.productoFinanciero.service;

import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroRequest;
import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroResponse;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;

import java.util.List;

public interface IProductoFinancieroService {
    ProductoFinancieroResponse createProducto(ProductoFinancieroRequest request);
    List<ProductoFinancieroResponse> getProductos();
    List<ProductoFinancieroResponse> getProductosByEstado(EstadoProducto estado);
    ProductoFinancieroResponse getProductoById(Long id);
    ProductoFinancieroResponse updateProducto(Long id, ProductoFinancieroRequest request);
    ProductoFinancieroResponse cambiarEstado(Long id, EstadoProducto estado);
}
