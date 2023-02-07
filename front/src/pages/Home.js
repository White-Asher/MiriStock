import './css/Home.css';
import HomeFavorite from './HomeFavorite';
import HomeMain from './HomeMain';
import { Routes, Route } from 'react-router-dom';
import { useEffect } from 'react';
import { memberAPI } from '../api/api';
import { userStore } from '../store';

const Home = () => {
  const { user, setUser } = userStore((state) => state);
  //일단 마운트될때마다로 설정 추후에 데이변할때 하게 해야함
  useEffect(() => {
    memberAPI
      .asset()
      .then((request) => {
        setUser(request.data);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);
  return (
    <div className="main-container">
      <Routes>
        <Route path="" element={<HomeMain />} />;
        <Route path="homeFavorite" element={<HomeFavorite />} />;
      </Routes>
    </div>
  );
};

export default Home;