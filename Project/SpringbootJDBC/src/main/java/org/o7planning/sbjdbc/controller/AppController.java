package org.o7planning.sbjdbc.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.o7planning.sbjdbc.dao.AuthorDao;
import org.o7planning.sbjdbc.dao.BookDao;
import org.o7planning.sbjdbc.dao.PublisherDao;
import org.o7planning.sbjdbc.model.Cart;
import org.o7planning.sbjdbc.model.author;
import org.o7planning.sbjdbc.model.books;
import org.o7planning.sbjdbc.model.cardbook;
import org.o7planning.sbjdbc.model.publisher;
import org.o7planning.sbjdbc.model.users;
import org.o7planning.sbjdbc.services.AuthorServices;
import org.o7planning.sbjdbc.services.CardService;
import org.o7planning.sbjdbc.services.PublisherServices;
import org.o7planning.sbjdbc.services.SachServices;
import org.o7planning.sbjdbc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppController {
	HashMap<Integer, users> infor;

	@Autowired
	private SachServices sachsv;
	@Autowired
	private BookDao sachDao;
	@Autowired
	private AuthorServices authorsv;
	@Autowired
	private AuthorDao authorDao;
	@Autowired
	private PublisherServices pubsv;

	@Autowired
	private UserService ussv;
	@Autowired
	private PublisherDao pubDao;
	@Autowired
	private CardService cardsv;

	@RequestMapping(value = { "/", "/index" })
	public String index() {
		
		return "index";
	}

	@RequestMapping("/about")
	public String ThongTin() {
		
		return "about";
	}

	@RequestMapping("/muonsach")
	public String MuonSach() {
		
		return "muonsach";
	}

	@RequestMapping("/contact.html")
	public String LienLac(Model model) {
		model.addAttribute("findbook", new books());
		return "contact";
	}

	@RequestMapping("/login.html")
	public String DangKy(Model model) {
		model.addAttribute("us", new users());
		model.addAttribute("uss", new users());
		return "login";
	}

	@RequestMapping("/contact")
	public String LienLacc() {
		return "contact";
	}

	// Xem chi tiet sach
	@RequestMapping(value = "/chitiet", method = RequestMethod.GET)
	public String showChiTiet(@RequestParam("id") Integer someId, Model model) {
		model.addAttribute("findbook", new books());
		Optional<books> userEdit = sachsv.findBookById(someId);
		userEdit.ifPresent(user -> model.addAttribute("sachs", user));
		return "chitiet";
	}

	// Load trang tu sach
	@RequestMapping(value = { "/book" })
	public String showBook(Model model, HttpServletRequest request, RedirectAttributes redirect) {
		request.getSession().setAttribute("booklist", null);
		
		if (model.asMap().get("success") != null)
			redirect.addFlashAttribute("success", model.asMap().get("success").toString());
		return "redirect:/book/page/1";
	}

	// Phan trang de load sach
	@GetMapping("/book/page/{pageNumber}")
	public String showEmployeePage(HttpServletRequest request, @PathVariable int pageNumber, Model model) {
		
		PagedListHolder<?> pages = (PagedListHolder<?>) request.getSession().getAttribute("booklist");
		int pagesize = 10;

		List<books> list = (List<books>) sachDao.getSach();
		System.out.println(list.size());
		if (pages == null) {
			pages = new PagedListHolder<>(list);
			pages.setPageSize(pagesize);
		} else {
			final int goToPage = pageNumber - 1;
			if (goToPage <= pages.getPageCount() && goToPage >= 0)
				pages.setPage(goToPage);
		}

		request.getSession().setAttribute("booklist", pages);
		int current = pages.getPage() + 1;
		int begin = Math.max(1, current - list.size());
		int end = Math.min(begin + 5, pages.getPageCount());
		int totalPageCount = pages.getPageCount();
		String baseUrl = "/book/page/";

		model.addAttribute("beginIndex", begin);
		model.addAttribute("endIndex", end);
		model.addAttribute("currentIndex", current);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("baseUrl", baseUrl);
		model.addAttribute("sachs", pages);

		return "book_lists";
	}

	// CRUDbook
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String editUser(@RequestParam("id") Integer bookId, Model model) {
		
		Optional<books> userEdit = sachsv.findBookById(bookId);
		userEdit.ifPresent(user -> model.addAttribute("us", user));
		return "EditBook";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@Valid books us, BindingResult result, RedirectAttributes redirect, Model model,@RequestParam(value="author",required=false) String author,@RequestParam(value="publisher",required=false) String pub) {

		if (result.hasErrors()) {
			return "form";
		}
		

			 // reading the user input  
		if(author!=null&& pub!=null) { 
	       int idauthor=Integer.parseInt(author);
	       us.setIdauthor(idauthor);
	       int idpub=Integer.parseInt(pub);
	       us.setIdpublisher(idpub);
		}
		String image ="images/";
		image+=us.getImage();
		us.setImage(image);
		
		sachsv.saveBook(us);
		System.out.println(us.getIdauthor());
		redirect.addFlashAttribute("success", "Saved book successfully!");
		model.addAttribute("findbook", new books());
		return "redirect:/CRUDbook";
	}

	@RequestMapping("/CRUDbook")
	public String load(Model model) {
		List<books> list = (List<books>) sachDao.getSach();
		model.addAttribute("books", list);
		
		return "CRUDbook";
	}

	@GetMapping("/add")
	public String create(Model model) {
		model.addAttribute("us", new books());
		
		return "form";
	}

	@GetMapping("/delete")
	public String delete(@RequestParam("id") Integer bookId, RedirectAttributes redirect, Model model) {
		Optional<books> userEdit = sachsv.findBookById(bookId);
		userEdit.ifPresent(user -> sachsv.deleteBook(user.getId()));
		redirect.addFlashAttribute("success", "Deleted employee successfully!");
		
		return "redirect:/CRUDbook";
	}

	// CRUD author
	@RequestMapping(value = "/editauthor", method = RequestMethod.GET)
	public String editAuthor(@RequestParam("id") Integer bookId,Model model) {
		Optional<author> userEdit = authorsv.findAuthorById(bookId);
		userEdit.ifPresent(user -> model.addAttribute("us", user));
		
		return "editauthor";
	}

	@RequestMapping(value = "/saveauthor", method = RequestMethod.POST)
	public String saveauthor(@Valid author us, BindingResult result, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return "formauthor";
		}
		authorsv.saveAuthor(us);
	
		redirect.addFlashAttribute("success", "Saved book successfully!");
		return "redirect:/CRUDauthor";
	}

	@RequestMapping("/CRUDauthor")
	public String loadauthor(Model model) {
		List<author> list = (List<author>) authorDao.getAuthor();
		model.addAttribute("authors", list);
	
		return "CRUDauthor";
	}

	@GetMapping("/addauthor")
	public String createauthor(Model model) {
		model.addAttribute("us", new author());
	
		return "formauthor";
	}

	@GetMapping("/deleteauthor")
	public String deleteauthor(@RequestParam("id") Integer authorId, RedirectAttributes redirect) {
		Optional<author> userEdit = authorsv.findAuthorById(authorId);
		userEdit.ifPresent(user -> authorsv.deleteAuthor(user.getId()));
		
		redirect.addFlashAttribute("success", "Deleted employee successfully!");
		return "redirect:/CRUDauthor";
	}

	// CRUD publisher
	@RequestMapping(value = "/editpub", method = RequestMethod.GET)
	public String editPub(@RequestParam("id") Integer pubId, Model model) {
		Optional<publisher> userEdit = pubsv.findPublisherById(pubId);
		userEdit.ifPresent(user -> model.addAttribute("us", user));
		model.addAttribute("findbook", new books());
		return "editpublisher";
	}

	@RequestMapping(value = "/savepub", method = RequestMethod.POST)
	public String savepub(@Valid publisher us, BindingResult result, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return "formpublisher";
		}
		pubsv.savePublisher(us);
		redirect.addFlashAttribute("success", "Saved book successfully!");
		return "redirect:/CRUDpublisher";
	}

	@RequestMapping("/CRUDpublisher")
	public String loadpublisher(Model model) {
		List<publisher> list = (List<publisher>) pubDao.getPub();
		model.addAttribute("publishers", list);
		return "CRUDpublisher";
	}

	@GetMapping("/addpub")
	public String createpub(Model model) {
		model.addAttribute("us", new publisher());
		return "formpublisher";
	}

	@GetMapping("/deletepub")
	public String deletepub(@RequestParam("id") Integer pubId, RedirectAttributes redirect) {
		Optional<publisher> userEdit = pubsv.findPublisherById(pubId);
		userEdit.ifPresent(user -> pubsv.deletePublisher(user.getId()));
		redirect.addFlashAttribute("success", "Deleted employee successfully!");
		return "redirect:/CRUDpublisher";
	}

	// register user
	@RequestMapping(value = "/saveuser", method = RequestMethod.POST)
	public String saveuser(@Valid users us, BindingResult result, RedirectAttributes redirect, Model model) {
		String email = us.getEmail();
		Optional<users> userEdit = ussv.finduserByemail(email);
		if (userEdit.isPresent() == false) {
			if (us.checkEmail()) {
				us.setRole(0);
				ussv.saveUser(us);
				model.addAttribute("us", us);
				model.addAttribute("uss", new users());
				redirect.addFlashAttribute("success", "Saved user successfully!");
				return "rgsucess";
			} else {
				model.addAttribute("us", new users());
				model.addAttribute("uss", new users());
				redirect.addFlashAttribute("fail", "Please choose another email");
				return "rgfailed";
			}

		} else {
			model.addAttribute("us", new users());
			model.addAttribute("uss", new users());
			redirect.addFlashAttribute("fail", "Please choose another email");
			return "rgfailed";

		}

	}

	@RequestMapping(value = "/loginuser", method = RequestMethod.POST)
	public String loginuser(@Valid users uss, BindingResult result, RedirectAttributes redirect, Model model,
			HttpSession session) {
		String email = uss.getEmail();
		String pass = uss.getPassword();
		/* HashMap<Integer, users> */ infor = new HashMap<>();
		
		users userEdit = ussv.findUser(email, pass);
		if (userEdit != null) {
			users userE = ussv.findusers(email);
			infor.put(userEdit.getId(), userEdit);
			session.setAttribute("user", infor);
			if (userE.getRole() == 1)
				return "manager";

			else
				return "userpage";

		} else {
			model.addAttribute("us", new users());
			model.addAttribute("uss", new users());
			redirect.addFlashAttribute("fail", "Please choose another email");
			return "loginfailed";

		}

	}

	// logout
	@RequestMapping(value = "/logout.html", method = RequestMethod.GET)
	public String Thoat(HttpSession session) {
		session.removeAttribute("user");
		session.removeAttribute("myCartItems");
		session.removeAttribute("myCartNum");
		return "index";
	}

	// manage book
	@RequestMapping("/publishermn.html")
	public String bkmb() {
		return "redirect:/CRUDpublisher";
	}

	@RequestMapping("/authormn.html")
	public String authormb() {
		return "redirect:/CRUDauthor";
	}

	@RequestMapping("/bookmn.html")
	public String bookmb() {
		return "redirect:/CRUDbook";
	}

	// Tạo trang hàng
	@Transactional
	@RequestMapping(value = "/add/{productId}", method = RequestMethod.POST)
	public String viewAdd(ModelMap mm, HttpSession session, @PathVariable("productId") Integer productId) {
		
		HashMap<Integer, Cart> cartItems = (HashMap<Integer, Cart>) session.getAttribute("myCartItems");
		if (cartItems == null) {
			cartItems = new HashMap<>();
		}
		books product = sachsv.findById(productId);
		if (product != null) {

			if (cartItems.containsKey(productId)) {
				Cart item = cartItems.get(productId);
				item.setProduct(product);
				//if(item.getQuantity()<(item.getProduct().getNumber()-item.getProduct().getNumberborrow()))
				     item.setQuantity(item.getQuantity() + 1);

			} else {
				Cart item = new Cart();
				item.setProduct(product);
				item.setQuantity(1);

				cartItems.put(productId, item);
				System.out.println("Sản phẩm chưa tồn tại");
				System.out.println(item.getQuantity());
			}

		}
		session.setAttribute("myCartItems", cartItems);
		session.setAttribute("myCartNum", cartItems.size());
		return "chitiet";

	}

	@RequestMapping(value = "/minus/{productId}", method = RequestMethod.POST)
	public String viewPlus(ModelMap mm, HttpSession session, @PathVariable("productId") Integer productId) {
		HashMap<Integer, Cart> cartItems = (HashMap<Integer, Cart>) session.getAttribute("myCartItems");
		if (cartItems == null) {
			cartItems = new HashMap<>();
		}
		books product = sachsv.findById(productId);
		if (product != null) {

			if (cartItems.containsKey(productId)) {
				Cart item = cartItems.get(productId);
				item.setProduct(product);
				item.setQuantity(item.getQuantity() - 1);
				if (item.getQuantity() == 0)
					cartItems.remove(productId);
			}

		}
		session.setAttribute("myCartItems", cartItems);
		session.setAttribute("myCartNum", cartItems.size());
		return "index";

	}

	@RequestMapping("/cart.html")
	public String Cart() {
		return "cart";
	}

	// Tạo nút remove
	@RequestMapping(value = "/remove/{id}", method = RequestMethod.POST)
	public String viewRemove(ModelMap mm, HttpSession session, @PathVariable("id") Integer productId) {
		HashMap<Integer, Cart> cartItems = (HashMap<Integer, Cart>) session.getAttribute("myCartItems");
		if (cartItems == null) {
			cartItems = new HashMap<>();
		}
		if (cartItems.containsKey(productId)) {
			cartItems.remove(productId);
		}
		session.setAttribute("myCartItems", cartItems);
		session.setAttribute("myCartNum", cartItems.size());

		return "cart";

	}

	@RequestMapping("/checkout.html")
	public String XacNhan(HttpSession session,HttpServletRequest request) {
		session=request.getSession(false);
		if(session==null) {
			return "index";
		}
		return "checkout";
	}

	@RequestMapping(value = "/confirm.html", method = RequestMethod.GET)
	public String firm(HttpSession session) {
		HashMap<Integer, Cart> cartItems = (HashMap<Integer, Cart>) session.getAttribute("myCartItems");
		Integer idusser = 0;
		Integer[] idbook = new Integer[10];
		String[] bookname = new String[10];
		int[] number = new int[10];
		int status = 0;
		int count = 0;

		if(cartItems==null)
			    return "index";
		if(infor==null) {
			return "index";
		}else {
			for (Map.Entry<Integer, users> list : infor.entrySet()) {
				idusser = list.getValue().getId();
			}
		int dem = 0;
		for (Map.Entry<Integer, Cart> list : cartItems.entrySet()) {
			idbook[dem] = list.getValue().getProduct().getId();
			bookname[dem] = list.getValue().getProduct().getBookname();
			number[dem] = list.getValue().getQuantity();
			dem++;

		}

		// update số lượng mượn

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		// Tính tổng số lượng sách
		for (int i = 0; i < dem; i++) {
			count += number[i];
			//

		}
		// Tìm xem user có mượn sách và đã trả rồi hay chưa và số lượng sách mượn nhỏ
		// hơn 4
		cardbook usth1 = cardsv.findByUserId(idusser, 1);
		cardbook usth2 = cardsv.findId(idusser);
		if ((usth1 == null && usth2 != null) || count >= 4) {

			return "Failedcheckout";

		} else {
			for (int i = 0; i < dem; i++) {

				cardbook card = new cardbook(idusser, idbook[i], bookname[i], currentTime, number[i], status);
				cardsv.saveCard(card);
				sachsv.updateBook(idbook[i], number[i]);
			}

			return "Sucesscheckout";
		}
		}

	}
	@RequestMapping(value = "/find", method = RequestMethod.POST)
	public String find(Model model,@RequestBody books bookname,HttpSession session) {
		//String name=request.getParameter("bookname");
		String tmp=bookname.getBookname();
		String name="%";
		name+=bookname.getBookname();	
		name+="%";
		String type=bookname.getType();
		if(type==""&&name!="") {
		books list =  sachsv.searchBook(name);
		if(list!=null)
       // model.addAttribute();
		  session.setAttribute("booklist",list);
		else {
       	 session.setAttribute("booklist",null);
        }	 
	 }else if(tmp=="" && type!="") {
		         List<books> typelist = sachsv.searchType(type);
		         if(typelist!=null)
		        	 session.setAttribute("booklist",typelist);
		         else {
		        	 session.setAttribute("booklist",null);
		         }	 
		        	 
		         
	  }else if(tmp!="" && type!="") {
		  books book = sachsv.searchNameType(name, type);
		  if(book!=null) {
			     System.out.println(type);
		        System.out.println(book.getType());
	        	 session.setAttribute("booklist",book);
		  }else {
	        	 session.setAttribute("booklist",null);
	      }	 
	  }
			
		 
        return "findbook";
		
		//System.out.println(list.getBookname());
		
	}
	@RequestMapping("/findbook")
	public String ViewBook() {
		return "findbook";
	}
	@RequestMapping("/PMS.html")
	public String ViewPMS() {
		return "PMS";
	}
	Integer maPMS=0;
	@RequestMapping(value = "/findPMS", method = RequestMethod.POST)
	public String find(Model model,@RequestBody cardbook id,HttpSession session) {
	            cardbook card = cardsv.findCard(id.getId());
	            if(card!=null) {
	            	session.setAttribute("Pms",card);
	            	maPMS=card.getId();
	            }else {
	            	session.setAttribute("Pms",null);
	            }
	            return "PMS";
	}
	@RequestMapping("/getPMS")
	public String get(Model model,HttpSession session) {
	          if(maPMS!=0) {
	        	  cardbook card = cardsv.findCard(maPMS);
	        	  if(card!=null) {
	        		  String oldDate = card.getRent_date();
	        		  System.out.println("Date before Addition: "+oldDate);
	        			//Specifying date format that matches the given date
	        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        			Calendar c = Calendar.getInstance();
	        			try{
	        			   //Setting the date to the given date
	        			   c.setTime(sdf.parse(oldDate));
	        			}catch(ParseException e){
	        				e.printStackTrace();
	        			 }
	        			   
	        			//Number of Days to add
	        			c.add(Calendar.DAY_OF_MONTH, 20);  
	        			//Date after adding the days to the given date
	        			String newDate = sdf.format(c.getTime()); 
	        			Date date1 = null;
	        			Date date2=null;
	        			Date now = new Date();
	        			String current = sdf.format(now); 
	        			try {
	        			date1 = sdf.parse(newDate);
	        	        date2 = sdf.parse(current);
	        			}catch(ParseException e) {
	        				e.printStackTrace();
	        			}
	        			//Displaying the new Date after addition of Days
	        			System.out.println("Date after Addition: "+newDate);
	        			 if (date1.compareTo(date2) < 0) {
	        				 cardsv.updateCard(card.getId(), 1);
	        				 int rentnumber=card.getRent_number();
	        				 books book = sachsv.findById(card.getIdbook());
	        				 int currentnum = book.getNumberborrow()-rentnumber;
	        				 //update số lượng sách mượn
	        				 sachsv.updateBook(book.getId(),currentnum);
	        				 
	        				      return "failedPMS";
	        			 }else {
	        		  cardsv.updateCard(card.getId(), 1);
	        		  int rentnumber=card.getRent_number();
     				 books book = sachsv.findById(card.getIdbook());
     				 int currentnum = book.getNumberborrow()-rentnumber;
     				 //update số lượng sách mượn
     				 sachsv.updateBook(book.getId(),currentnum);
	        		  return "successPMS";
	        			 }
	        	  }else {
	        		  return "PMS";
	        	  }
	          }else {
	        	  return "PMS";
	          }
	            
	           
	}
}
	