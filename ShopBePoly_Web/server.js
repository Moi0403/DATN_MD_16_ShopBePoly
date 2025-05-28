const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();
const app = express();
const port = 3000;

const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}));


const productModel = require('./Database/productModel');
const COMOMJS = require('./Database/COMOM');

const uri = COMOMJS.uri;

// Kết nối MongoDB
mongoose.connect(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000 // để không bị treo nếu không kết nối được
})
.then(() => console.log('Connected to MongoDB'))
.catch((err) => console.error('MongoDB connection error:', err));

// Khởi động server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});

app.get('/ds_product', async (req, res) => {
    try {
        const products = await productModel.find();
        console.log(products);
        res.json(products); // trả kết quả về client
    } catch (err) {
        console.error('Error fetching products:', err);
        res.status(500).json({ error: 'Lỗi khi lấy danh sách sản phẩm' });
    }
});

app.use('/api', router);

// lấy ds product 'http://localhost:3000/api/list_product'
router.get('/list_product', async (req, res)=>{
    await mongoose.connect(uri);
    let product = await productModel.find();
    res.send(product);
});

// thêm product 'http://localhost:3000/api/add_product'
router.post('/add_product', async (req, res)=>{
    
    let data = req.body;
    let kq = await productModel.create(data);

    if(kq){
        console.log('Thêm sản phẩm thành công');
        let pro = await productModel.find();
        res.send(pro);
    } else{
        console.log('Thêm sản phẩm không thành công');
    }

})

// sửa product 'http://localhost:3000/api/up_product/ id'
router.put('/up_product/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await productModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else{
            res.send('Không tìm thấy sản phẩm để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

// xóa sản phẩm 'http://localhost:3000/api/del_product/ id'
router.delete('/del_product/:id', async (req, res)=>{
    try{
        let id = req.params.id;
        const kq = await productModel.deleteOne({_id: id});
        if(kq){
            console.log('Xóa sản phẩm thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else{
            res.send('Xóa sản phẩm không thành công');
        }
    } catch(error){
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server khi xóa sản phẩm' });
    }
})

//User
// lấy ds user 'http://localhost:3000/api/list_user'
router.get('/list_user', async (req, res)=>{
    await mongoose.connect(uri);
    let user = await userModel.find();
    res.send(user);
});

// thêm user 'http://localhost:3000/api/add_user'
router.post('/add_user', async (req, res)=>{
    
    let data = req.body;
    let kq = await userModel.create(data);

    if(kq){
        console.log('Thêm người dùng thành công');
        let usr = await userModel.find();
        res.send(usr);
    } else{
        console.log('Thêm người dùng không thành công');
    }

})

// sửa user 'http://localhost:3000/api/up_user/ id'
router.put('/up_user/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await userModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else{
            res.send('Không tìm thấy người dùng để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

// xóa user 'http://localhost:3000/api/del_user/ id'
router.delete('/del_user/:id', async (req, res)=>{
    try{
        let id = req.params.id;
        const kq = await userModel.deleteOne({_id: id});
        if(kq){
            console.log('Xóa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else{
            res.send('Xóa không thành công');
        }
    } catch(error){
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server' });
    }
})

app.use(express.json()); // bắt buộc để đọc req.body
