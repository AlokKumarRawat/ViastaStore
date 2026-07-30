package com.project.Viastastore.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Products {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private long id;
		//pricing details
		@Column(nullable = false)
		private String productName;
		@Column(nullable = false,length = 100)
		private String productDescription;
		
		@ManyToOne
		private Category category;
		private String brandName;
		private String gender;     //kids,mens,womens

		//price details
		@Column(nullable = false)
		private double costPrice;
		@Column(nullable = false)
		private double sellingPrice;
		@Column(nullable = false)
		private int discount;
		@Column(nullable = false)
		private double finalPrice;
		
		//product varients
		@Column(nullable = false,length = 500)
		private String colors;  //black, white, red ,#fff , #111111
		@Column(nullable = false,length = 500)
		private String sizes;		//L,M,XL,XXL,S
		
		//Inventory
		@Column(nullable = false)
		private long quantity;
		
		private double shippingCharge;
		@Column(nullable = false)
		private String deliveryTime;
		
		private boolean returnPolicy;
		@Column(nullable = false)
		private boolean visiblity;
		
		private LocalDateTime addedAt;
		@ElementCollection
		private List<String> productImages;
		
		@Enumerated(EnumType.STRING)
		private ProductStatus status;
		
		public enum ProductStatus{
			Available,Out_Of_Stock
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public String getProductDescription() {
			return productDescription;
		}

		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}

		public Category getCategory() {
			return category;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		public String getBrandName() {
			return brandName;
		}

		public void setBrandName(String brandName) {
			this.brandName = brandName;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public double getCostPrice() {
			return costPrice;
		}

		public void setCostPrice(double costPrice) {
			this.costPrice = costPrice;
		}

		public double getSellingPrice() {
			return sellingPrice;
		}

		public void setSellingPrice(double sellingPrice) {
			this.sellingPrice = sellingPrice;
		}

		public int getDiscount() {
			return discount;
		}

		public void setDiscount(int discount) {
			this.discount = discount;
		}

		public double getFinalPrice() {
			return finalPrice;
		}

		public void setFinalPrice(double finalPrice) {
			this.finalPrice = finalPrice;
		}

		public String getColors() {
			return colors;
		}

		public void setColors(String colors) {
			this.colors = colors;
		}

		public String getSizes() {
			return sizes;
		}

		public void setSizes(String sizes) {
			this.sizes = sizes;
		}

		public long getQuantity() {
			return quantity;
		}

		public void setQuantity(long quantity) {
			this.quantity = quantity;
		}

		public double getShippingCharge() {
			return shippingCharge;
		}

		public void setShippingCharge(double shippingCharge) {
			this.shippingCharge = shippingCharge;
		}

		public String getDeliveryTime() {
			return deliveryTime;
		}

		public void setDeliveryTime(String deliveryTime) {
			this.deliveryTime = deliveryTime;
		}

		public boolean isReturnPolicy() {
			return returnPolicy;
		}

		public void setReturnPolicy(boolean returnPolicy) {
			this.returnPolicy = returnPolicy;
		}

		public boolean isVisiblity() {
			return visiblity;
		}

		public void setVisiblity(boolean visiblity) {
			this.visiblity = visiblity;
		}

		public LocalDateTime getAddedAt() {
			return addedAt;
		}

		public void setAddedAt(LocalDateTime addedAt) {
			this.addedAt = addedAt;
		}

		public List<String> getProductImages() {
			return productImages;
		}

		public void setProductImages(List<String> productImages) {
			this.productImages = productImages;
		}

		public ProductStatus getStatus() {
			return status;
		}

		public void setStatus(ProductStatus status) {
			this.status = status;
		}
		
		
}
