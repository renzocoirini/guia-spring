package dev.renzo.crud.controller;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.renzo.crud.dto.MensajeDTO;
import dev.renzo.crud.dto.ProductoDTO;
import dev.renzo.crud.entity.Producto;
import dev.renzo.crud.service.ProductService;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    private ProductService productService;
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Error: Product doesn't exist";

    @GetMapping("")
    public ResponseEntity<List<Producto>> findAll() {
        List<Producto> products = productService.list();
        return new ResponseEntity<List<Producto>>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") int id) {
        if (!productService.existsById(id))
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        Optional<Producto> optionalProduct = productService.getOne(id);
        if (!optionalProduct.isPresent())
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        Producto product = optionalProduct.get();
        return createProductResponse(product, HttpStatus.OK);
    }

    @GetMapping("/detail-name/{name}")
    public ResponseEntity<?> getOne(@PathVariable("name") String name) {
        if (!productService.existsByNombre(name))
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        Optional<Producto> optionalProduct = productService.getByNombre(name);
        if (!optionalProduct.isPresent())
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        Producto product = optionalProduct.get();
        return createProductResponse(product, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<MensajeDTO> create(@RequestBody ProductoDTO productDto) {
        if (StringUtils.isBlank(productDto.getNombre()))
            return createRequestResponse("Error: Product name is mandatory", HttpStatus.BAD_REQUEST);

        if (productDto.getPrecio() == null || productDto.getPrecio() < 0)
            return createRequestResponse("Error: Product price is mandatory", HttpStatus.BAD_REQUEST);

        if (productService.existsByNombre(productDto.getNombre()))
            return createRequestResponse("Error: Product name already exists", HttpStatus.CONFLICT);

        Producto product = new Producto(productDto.getNombre(), productDto.getPrecio());
        productService.save(product);
        return new ResponseEntity<>(new MensajeDTO("Product created successfully!"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO> update(@PathVariable("id") int id, @RequestBody ProductoDTO productDto) {

        Optional<Producto> optionalProductByName = productService.getByNombre(productDto.getNombre());

        if (!productService.existsById(id))
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        if (StringUtils.isBlank(productDto.getNombre()))
            return createRequestResponse("Error: Product name is mandatory", HttpStatus.BAD_REQUEST);

        if (productDto.getPrecio() == null || productDto.getPrecio() < 0)
            return createRequestResponse("Error: Product price is mandatory", HttpStatus.BAD_REQUEST);

        if (productService.existsByNombre(productDto.getNombre())
                && optionalProductByName.isPresent()
                && optionalProductByName.get().getId() != id)
            return createRequestResponse("Product name already exists", HttpStatus.CONFLICT);

        Optional<Producto> optionalProduct = productService.getOne(id);
        if (!optionalProduct.isPresent())
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        Producto product = optionalProduct.get();
        product.setNombre(productDto.getNombre());
        product.setPrecio(productDto.getPrecio());
        productService.save(product);
        return createRequestResponse("Product updated successfully!", HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO> delete(@PathVariable("id") int id) {
        if (!productService.existsById(id))
            return createRequestResponse(PRODUCT_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);

        productService.delete(id);
        return createRequestResponse("Product deleted successfully!", HttpStatus.OK);
    }

    private ResponseEntity<MensajeDTO> createRequestResponse(String message, HttpStatus httpStatus) {
        MensajeDTO errorMessage = new MensajeDTO(message);
        return new ResponseEntity<>(errorMessage, httpStatus);
    }

    private ResponseEntity<Producto> createProductResponse(Producto producto, HttpStatus httpStatus) {
        return new ResponseEntity<>(producto, httpStatus);
    }

}